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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

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
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    var pipeline = ch.pipeline();
                    pipeline.addLast("decoder", packetDecoder);
                    pipeline.addLast("encoder", packetEncoder);
                    pipeline.addLast("handler", inboundHandler);
                }
            });
    }

    @Override
    protected void start(InetSocketAddress address) throws Exception {
        CountDownLatch connectionLatch = new CountDownLatch(1);

        thread = new Thread(() -> {
            try {
                ChannelFuture channelFuture = bootstrap.connect(address).sync();
                clientChannel = channelFuture.channel();

                connectionLatch.countDown();
                log.info("Connected to server at {}", address);

                // Wait for the channel to close
                clientChannel.closeFuture().sync();
            } catch (Exception e) {
                log.error("Error connecting to server", e);
            }
        });

        thread.setName("NettyClientThread");
        thread.setDaemon(true);
        thread.start();

        // Wait for the connection to be established
        connectionLatch.await();
    }
    
    /**
     * Connects to a server and returns a CompletableFuture that resolves to the connection's HermesChannel
     */
    public void connect(InetSocketAddress address) {
        CompletableFuture<HermesChannel> future = new CompletableFuture<>();

        try {
            bootstrap.connect(address).addListener((ChannelFuture channelFuture) -> {
                if (channelFuture.isSuccess()) {
                    clientChannel = channelFuture.channel();
                    
                    // Create a new Hermes channel for this connection
                    HermesChannel hermesChannel = new HermesChannel();
                    // Register the mapping between Hermes and Netty channels
                    registerChannel(hermesChannel, clientChannel);
                    
                    log.info("Connected to server at {}", address);
                    future.complete(hermesChannel);
                } else {
                    log.error("Failed to connect to server at {}", address, channelFuture.cause());
                    future.completeExceptionally(channelFuture.cause());
                }
            });
        } catch (Exception e) {
            log.error("Error connecting to server at {}", address, e);
            future.completeExceptionally(e);
        }
        
        future.join();
    }

    @Override
    public void sendPacket(Packet<?> packet) throws Exception {
        HermesChannel hermesChannel = getHermesChannel(clientChannel);
        sendPacket(hermesChannel, packet);
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
            group.shutdownGracefully().sync();
            log.info("Netty client shut down gracefully");
        } catch (InterruptedException e) {
            log.error("Error shutting down Netty client", e);
            Thread.currentThread().interrupt();
        }
    }
}