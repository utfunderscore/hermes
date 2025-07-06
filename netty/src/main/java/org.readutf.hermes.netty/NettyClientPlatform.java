package org.readutf.hermes.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.jetbrains.annotations.NotNull;
import org.readutf.hermes.codec.PacketCodec;
import org.readutf.hermes.packet.Packet;
import org.readutf.hermes.platform.HermesChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class NettyClientPlatform extends NettyPlatform {

    private static final Logger log = LoggerFactory.getLogger(NettyClientPlatform.class);
    private @NotNull final EventLoopGroup group;
    private @NotNull final Bootstrap bootstrap;
    private Channel clientChannel;

    public NettyClientPlatform(@NotNull PacketCodec codec) {
        super(codec);
        this.group = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        initializeBootstrap();
    }

    @Override
    protected void initializeBootstrap() {
        InboundHandler inboundHandler = new InboundHandler(this);
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                var pipeline = ch.pipeline();
                pipeline.addLast("decoder", new PacketDecoder(getCodec()));
                pipeline.addLast("encoder", new PacketEncoder(getCodec()));
                pipeline.addLast("handler", inboundHandler);
            }
        });
    }

    @Override
    protected void start(InetSocketAddress address) throws Exception {}

    /**
     * Connects to a server and returns a CompletableFuture that resolves to the connection's HermesChannel
     */
    public void connect(InetSocketAddress address, long timeoutMillis) throws Exception {
        CompletableFuture<Void> future = new CompletableFuture<>();

        thread = new Task(future, address);

        thread.setName("NettyClientThread");
        thread.setDaemon(true);
        thread.start();

        // Wait for the connection to be established
        future.get(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void sendPacket(Packet<?> packet) throws Exception {
        HermesChannel hermesChannel = getHermesChannel(clientChannel);
        sendPacket(hermesChannel, packet);
    }

    public void awaitShutdown() throws InterruptedException {
        if (thread != null) {
            thread.join();
        }
    }

    /**
     * Disconnects from the server
     */
    public void disconnect() {
        if (clientChannel != null && clientChannel.isOpen()) {
            clientChannel.close();
            log.info("Disconnected from server");
        }
    }

    @Override
    public void shutdown() {
        try {
            if (clientChannel != null) {
                clientChannel.close().sync();
            }
            group.shutdownGracefully().syncUninterruptibly();
            log.info("Netty client shut down gracefully");
        } catch (InterruptedException e) {
            log.error("Error shutting down Netty client", e);
            Thread.currentThread().interrupt();
        }
    }

    public class Task extends Thread {

        private final CompletableFuture<Void> connectionLatch;
        private final InetSocketAddress address;

        public Task(CompletableFuture<Void> connectionLatch, InetSocketAddress address) {
            this.connectionLatch = connectionLatch;
            this.address = address;
        }

        @Override
        public void run() {
            try {
                log.info("Attempting to connect to server at {}", address);
                ChannelFuture channelFuture = bootstrap.connect(address).sync();
                clientChannel = channelFuture.channel();
                connectionLatch.complete(null);

                log.info("Connected to server at {}", address);

                // Wait for the channel to close
                log.info("Waiting for channel to close...");
                clientChannel.closeFuture().sync();
                log.info("Channel closed, shutting down client");
            } catch (Exception e) {
                connectionLatch.completeExceptionally(e);
            }
            log.info("Thread {} finished execution", Thread.currentThread().getName());
            shutdown();
        }
    }
}