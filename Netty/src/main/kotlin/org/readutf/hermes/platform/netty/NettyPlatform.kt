package org.readutf.hermes.platform.netty

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.bootstrap.AbstractBootstrap
import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import org.readutf.hermes.PacketManager
import org.readutf.hermes.platform.PacketPlatform
import org.readutf.hermes.serializer.PacketSerializer
import org.readutf.hermes.Packet
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

abstract class NettyPlatform internal constructor(
    internal val hostName: String,
    internal val port: Int,
    internal var serializer: PacketSerializer,
    internal var bootstrap: AbstractBootstrap<*, *>,
) : ChannelInboundHandlerAdapter(),
    PacketPlatform {
    val logger = KotlinLogging.logger { }
    private var thread: Thread? = null
    lateinit var packetConsumer: Consumer<Packet>
    lateinit var channel: Channel

    var activeChannels = mutableSetOf<Channel>()

    fun getChannelInitializer(): ChannelInitializer<SocketChannel> =
        object : ChannelInitializer<SocketChannel>() {
            var logger = KotlinLogging.logger {}

            override fun initChannel(socketChannel: SocketChannel) {
                logger.info { "New connection from ${socketChannel.remoteAddress()}" }

                val pipeline = socketChannel.pipeline()
                pipeline.addLast("decoder", NettyPacketDecoder(serializer))
                pipeline.addLast("encoder", NettyPacketEncoder(serializer))
                pipeline.addLast("handler", NettyInboundHandler(this@NettyPlatform))
            }
        }

    fun handlePacket(packet: Packet) {
        if (::packetConsumer.isInitialized) packetConsumer.accept(packet)
    }

    override fun setupPacketListener(packetConsumer: Consumer<Packet>) {
        this.packetConsumer = packetConsumer
    }

    override fun sendPacket(packet: Packet) {
        channel.writeAndFlush(packet)
    }

    override fun setSerializer(serializer: PacketSerializer) {
        this.serializer = serializer
    }
}

class NettyServerPlatform(
    hostName: String,
    port: Int,
    serializer: PacketSerializer,
    bootstrap: ServerBootstrap,
) : NettyPlatform(
        hostName,
        port,
        serializer,
        bootstrap,
    ) {
    private lateinit var thread: Thread

    override fun start() {
        Thread {
            bootstrap =
                (bootstrap as ServerBootstrap)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(getChannelInitializer())

            val channelFuture =
                bootstrap
                    .bind(hostName, port)
                    .sync()

            logger.info { "Server started on $hostName:$port" }

            channelFuture.channel().closeFuture()
        }.start()
    }

    override fun stop() {
        if (::thread.isInitialized) thread.interrupt()
    }
}

class NettyClientPlatform(
    hostName: String,
    port: Int,
    serializer: PacketSerializer,
    bootstrap: Bootstrap,
) : NettyPlatform(
        hostName,
        port,
        serializer,
        bootstrap,
    ) {
    private lateinit var thread: Thread

    override fun start() {
        val startFuture = CompletableFuture<Channel>()
        Thread {
            val channel =
                (bootstrap as Bootstrap)
                    .channel(NioSocketChannel::class.java)
                    .handler(getChannelInitializer())
                    .connect(hostName, port)
                    .sync()
                    .channel()

            this.channel = channel

            startFuture.complete(channel)

            logger.info { "Connected to $hostName:$port" }

            channel
                .closeFuture()
                .sync()
        }.start()

        startFuture.join()
    }

    override fun stop() {
        if (::thread.isInitialized) thread.interrupt()
    }
}

fun PacketManager.Companion.nettyServer(
    hostName: String = "localhost",
    port: Int = 4000,
    serializer: PacketSerializer,
    serverBootstrap: ServerBootstrap = ServerBootstrap().group(NioEventLoopGroup(), NioEventLoopGroup()),
): PacketManager<NettyServerPlatform> {
    val platform = NettyServerPlatform(hostName, port, serializer, serverBootstrap)
    return create(platform)
}

fun PacketManager.Companion.nettyClient(
    hostName: String = "localhost",
    port: Int = 4000,
    serializer: PacketSerializer,
    serverBootstrap: Bootstrap = Bootstrap().group(NioEventLoopGroup()),
): PacketManager<NettyClientPlatform> {
    val platform = NettyClientPlatform(hostName, port, serializer, serverBootstrap)
    return create(platform)
}
