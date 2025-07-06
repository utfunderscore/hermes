package org.readutf.hermes.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.jetbrains.annotations.NotNull;
import org.readutf.hermes.codec.PacketCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

public class NettyServerPlatform extends NettyPlatform {

    private static final Logger log = LoggerFactory.getLogger(NettyServerPlatform.class);
    private @NotNull final NioEventLoopGroup bossGroup;
    private @NotNull final NioEventLoopGroup workerGroup;
    private @NotNull final ServerBootstrap serverBootstrap;

    public NettyServerPlatform(@NotNull PacketCodec codec) {
        super(codec);
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.serverBootstrap = new ServerBootstrap();
        initializeBootstrap();
    }

    @Override
    protected void initializeBootstrap() {
        Supplier<InboundHandler> inboundHandlerSupplier = () -> new InboundHandler(this);
        serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        var pipeline = socketChannel.pipeline();
                        pipeline.addLast("decoder", new PacketEncoder(getCodec()));
                        pipeline.addLast("encoder", new PacketDecoder(getCodec()));
                        pipeline.addLast("handler", inboundHandlerSupplier.get());
                    }
                });
    }

    @Override
    public void start(InetSocketAddress address) throws Exception {
        CompletableFuture<Void> future = new CompletableFuture<>();

        thread = new Thread(() -> {
            try {
                var channelFuture = serverBootstrap.bind(address).sync();

                future.complete(null);
                log.info("Netty server started on {}", address);

                // Wait until the server socket is closed
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                log.error("Error initializing Netty server", e);
                shutdown();
                future.completeExceptionally(e);
            }
        });

        thread.setName("NettyServerThread");
        thread.setDaemon(true);
        thread.start();

        // Wait for the server to start
        future.join();
    }
    
    @Override
    public void shutdown() {
        try {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            log.info("Netty server shut down gracefully");
        } catch (InterruptedException e) {
            log.error("Error shutting down Netty server", e);
            Thread.currentThread().interrupt();
        }
    }
}