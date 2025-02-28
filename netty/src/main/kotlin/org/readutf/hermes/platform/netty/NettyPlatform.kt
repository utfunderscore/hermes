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
import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.platform.PacketPlatform
import org.readutf.hermes.serializer.PacketSerializer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool
import java.util.function.BiConsumer

abstract class NettyPlatform internal constructor(
    internal val hostName: String,
    internal val port: Int,
    internal var serializer: PacketSerializer,
    internal var bootstrap: AbstractBootstrap<*, *>,
) : ChannelInboundHandlerAdapter(),
    PacketPlatform {
    val logger = KotlinLogging.logger { }

    private lateinit var packetConsumer: BiConsumer<HermesChannel, Packet<*>>
    lateinit var channel: Channel
    lateinit var packetManager: PacketManager<NettyPlatform>

    private val channelMap = mutableMapOf<Channel, HermesChannel>()
    private val channelIdMap = mutableMapOf<String, HermesChannel>()

    var activeChannels = mutableMapOf<String, Channel>()

    override fun init(packetManager: PacketManager<*>) {
        this.packetManager = packetManager as PacketManager<NettyPlatform>
    }

    fun getChannelInitializer(): ChannelInitializer<SocketChannel> =
        object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(socketChannel: SocketChannel) {
                val pipeline = socketChannel.pipeline()
                pipeline.addLast("decoder", NettyPacketDecoder(serializer))
                pipeline.addLast("encoder", NettyPacketEncoder(serializer))
                pipeline.addLast("handler", NettyInboundHandler(this@NettyPlatform))
            }
        }

    fun handlePacket(
        hermesChannel: HermesChannel,
        packet: Packet<*>,
    ) {
        if (::packetConsumer.isInitialized) packetConsumer.accept(hermesChannel, packet)
    }

    fun getChannel(channel: Channel): HermesChannel = channelMap.getOrPut(channel) { NettyHermesChannel(channel, packetManager) }

    fun removeChannel(channel: Channel) = channelMap.remove(channel)

    override fun setupPacketListener(packetConsumer: BiConsumer<HermesChannel, Packet<*>>) {
        this.packetConsumer = packetConsumer
    }

    override fun getChannel(channelId: String): HermesChannel? = channelIdMap[channelId]

    fun getChannels(): Collection<Channel> = activeChannels.values

    override fun sendPacket(packet: Packet<*>) {
        if (::channel.isInitialized) {
            logger.debug { "Writing packet $packet and flushing..." }
            channel.writeAndFlush(packet)
        } else {
            logger.warn { "Channel not initialized, cannot send packet" }
        }
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
    private lateinit var bossGroup: NioEventLoopGroup
    private lateinit var workerGroup: NioEventLoopGroup

    override fun start() {
        bossGroup = NioEventLoopGroup()
        workerGroup = NioEventLoopGroup()

        thread =
            Thread({
                try {
                    bootstrap =
                        (bootstrap as ServerBootstrap)
                            .group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel::class.java)
                            .childHandler(getChannelInitializer())

                    val channelFuture =
                        bootstrap
                            .bind(hostName, port)
                            .sync()

                    logger.info { "Server started on $hostName:$port" }

                    channelFuture.channel().closeFuture()
                } catch (e: Exception) {
                    logger.error(e) { "Exception occurred on main netty thread" }
                }
            }, "Hermes-Netty-Thread")
        thread.start()
    }

    override fun stop() {
        if (::thread.isInitialized) {
            logger.debug { "Stopping netty platform thread" }
            thread.interrupt()
        }
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
    private lateinit var group: NioEventLoopGroup

    override fun start() {
        group = NioEventLoopGroup(1)

        val startFuture = CompletableFuture<Channel>()
        thread =
            Thread({
                try {
                    val channel =
                        try {
                            (bootstrap as Bootstrap)
                                .channel(NioSocketChannel::class.java)
                                .group(group)
                                .handler(getChannelInitializer())
                                .connect(hostName, port)
                                .sync()
                                .channel()
                        } catch (e: Exception) {
                            startFuture.completeExceptionally(e)
                            return@Thread
                        }

                    this.channel = channel

                    logger.info { "Connected to $hostName:$port" }
                    startFuture.complete(channel)

                    channel
                        .closeFuture()
                        .sync()
                } catch (e: Exception) {
                    logger.error(e) { "Exception occured on main netty thread" }
                } finally {
                    group.shutdownGracefully()
                }
            }, "Hermes-Client-Thread")
        thread.start()

        startFuture.join()
    }

    override fun stop() {
        if (::thread.isInitialized) {
            logger.debug { "Stopping netty platform thread" }
            thread.interrupt()
        }
    }
}

fun PacketManager.Companion.nettyServer(
    hostName: String = "localhost",
    port: Int = 4000,
    serializer: PacketSerializer,
    serverBootstrap: ServerBootstrap = ServerBootstrap(),
    executorService: ExecutorService = ForkJoinPool.commonPool(),
): PacketManager<NettyServerPlatform> {
    val platform = NettyServerPlatform(hostName, port, serializer, serverBootstrap)
    return create(platform, executorService)
}

fun PacketManager.Companion.nettyClient(
    hostName: String = "localhost",
    port: Int = 4000,
    serializer: PacketSerializer,
    serverBootstrap: Bootstrap = Bootstrap(),
    executorService: ExecutorService = ForkJoinPool.commonPool(),
): PacketManager<NettyClientPlatform> {
    val platform = NettyClientPlatform(hostName, port, serializer, serverBootstrap)
    return create(platform, executorService)
}
