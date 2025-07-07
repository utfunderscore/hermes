package org.readutf.hermes.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
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
        log.debug("NettyClientPlatform initialized with codec: {}", codec.getClass().getName());
        initializeBootstrap();
    }

    @Override
    protected void initializeBootstrap() {
        log.debug("Initializing Netty bootstrap...");
        InboundHandler inboundHandler = new InboundHandler(this);
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                var pipeline = ch.pipeline();
                pipeline.addLast("decoder", new PacketDecoder(getCodec()));
                pipeline.addLast("encoder", new PacketEncoder(getCodec()));
                pipeline.addLast("handler", inboundHandler);
                log.trace("Pipeline initialized for channel {} with handlers: decoder, encoder, handler", ch.id());
            }
        });
        log.trace("Bootstrap initialized");
    }

    @Override
    protected void start(InetSocketAddress address) throws Exception {
        log.warn("start(address) method is not implemented for NettyClientPlatform");
    }

    /**
     * Connects to a server and returns a CompletableFuture that resolves to the connection's HermesChannel
     */
    public void connect(InetSocketAddress address) throws Exception {
        log.debug("Initiating connection to server at {}", address);
        CompletableFuture<Void> future = new CompletableFuture<>();

        thread = new Task(future, address);

        thread.setName("NettyClientThread");
        thread.setDaemon(true);
        log.debug("Starting NettyClientThread for server connection");
        thread.start();

        // Wait for the connection to be established
        future.join();
        log.info("Connection future join completed");
    }

    @Override
    public void sendPacket(Packet<?> packet) throws Exception {
        if (clientChannel == null || !clientChannel.isActive()) {
            log.error("Attempted to send packet but client channel is not connected/active");
            throw new IllegalStateException("Client is not connected");
        }
        HermesChannel hermesChannel = getHermesChannel(clientChannel);
        log.debug("Sending packet {} on HermesChannel {}", packet.getClass().getSimpleName(), hermesChannel);
        sendPacket(hermesChannel, packet);
    }

    public void awaitShutdown() throws InterruptedException {
        if (thread != null) {
            log.trace("Awaiting client thread shutdown...");
            thread.join();
            log.trace("Client thread has terminated");
        } else {
            log.debug("awaitShutdown called but client thread is null");
        }
    }

    public boolean isConnected() {
        return clientChannel != null && clientChannel.isActive();
    }

    /**
     *
     *
     * Disconnects from the server
     */
    public void disconnect() {
        if (clientChannel != null && clientChannel.isOpen()) {
            log.info("Disconnecting from server...");
            clientChannel.close();
            log.info("Disconnected from server");
        } else {
            log.warn("Attempted disconnect but client channel is already closed or null");
        }
    }

    @Override
    public void shutdown() {
        try {
            if (clientChannel != null) {
                log.debug("Closing client channel...");
                clientChannel.close().sync();
                log.debug("Client channel closed");
            }
            log.info("Shutting down Netty event loop group...");
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
            // Set your desired timeout in milliseconds (e.g., 5000 ms = 5 seconds)
            int connectTimeoutMillis = 5000;
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis);

            try {
                log.info("Attempting to connect to server at {}", address);

                ChannelFuture channelFuture = bootstrap.connect(address);
                boolean connected = channelFuture.await(connectTimeoutMillis, TimeUnit.MILLISECONDS);

                if (!connected || !channelFuture.isSuccess()) {
                    Exception cause = channelFuture.cause() instanceof Exception ? (Exception) channelFuture.cause() : new Exception("Connection timed out");
                    log.error("Connection timed out or failed to server at {}. Cause: {}", address, cause.toString());
                    connectionLatch.completeExceptionally(cause);
                    return;
                }

                clientChannel = channelFuture.channel();
                connectionLatch.complete(null);

                log.info("Connected to server at {}", address);

                // Wait for the channel to close
                log.info("Waiting for channel to close...");
                clientChannel.closeFuture().sync();
                log.info("Channel closed, shutting down client");
            } catch (Exception e) {
                log.error("Failed to connect to server at {}: {}", address, e.toString(), e);
                connectionLatch.completeExceptionally(e);
            } finally {
                log.debug("Thread {} finished execution", Thread.currentThread().getName());
                shutdown();
            }
        }
    }
}