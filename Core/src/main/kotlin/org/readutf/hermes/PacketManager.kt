package org.readutf.hermes

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.exceptions.ExceptionManager
import org.readutf.hermes.listeners.ListenerManager
import org.readutf.hermes.platform.PacketPlatform
import org.readutf.hermes.response.ResponseListener
import org.readutf.hermes.response.ResponsePacket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.function.Consumer

class PacketManager<T : PacketPlatform>(
    val packetPlatform: T,
    val executorService: ExecutorService,
) {
    val logger = KotlinLogging.logger { }
    private val listenerManager = ListenerManager(executorService)
    private val exceptionManager = ExceptionManager()
    val responseFutures = mutableMapOf<Int, CompletableFuture<ResponsePacket>>()

    init {
        packetPlatform.setupPacketListener { channel, packet ->
            onPacketReceived(channel, packet)
        }
        packetPlatform.init(this)
        editListeners {
            it.registerListener(ResponseListener(this))
        }
    }

    fun sendPacket(packet: Packet) {
        packetPlatform.sendPacket(packet)
    }

    inline fun <reified T> sendPacket(packet: Packet): CompletableFuture<T> {
        val future = CompletableFuture<ResponsePacket>()
        responseFutures[packet.packetId] = future
        packetPlatform.sendPacket(packet)
        return future.thenApplyAsync({ responsePacket ->
            if (responsePacket is T) {
                logger.debug { "Received back $responsePacket as ${T::class.java.simpleName}" }
                return@thenApplyAsync responsePacket
            } else {
                throw IllegalStateException("Response packet was not of type ${T::class.java.simpleName}")
            }
        }, executorService)
    }

    fun start(): PacketManager<T> {
        packetPlatform.start()
        return this
    }

    fun exception(consumer: Consumer<Throwable>): PacketManager<T> {
        exceptionManager.setGlobalExceptionHandler(consumer)
        return this
    }

    fun <U : Throwable> exception(
        clazz: Class<U>,
        consumer: Consumer<U>,
    ): PacketManager<T> {
        exceptionManager.setExceptionHandler(clazz, consumer)
        return this
    }

    fun handleException(throwable: Throwable): Boolean = exceptionManager.handleException(throwable)

    fun editListeners(consumer: Consumer<ListenerManager>): PacketManager<T> {
        consumer.accept(listenerManager)
        return this
    }

    private fun onPacketReceived(
        hermesChannel: HermesChannel,
        packet: Packet,
    ) {
        logger.debug { "Received packet: $packet" }
        listenerManager.handlePacket(hermesChannel, packet)
    }

    fun stop() {
        packetPlatform.stop()
    }

    companion object {
        fun <T : PacketPlatform> create(
            platform: T,
            executorService: ExecutorService,
        ): PacketManager<T> = PacketManager(platform, executorService)
    }
}
