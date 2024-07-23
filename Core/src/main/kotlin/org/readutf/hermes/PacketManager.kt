package org.readutf.hermes

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import java.util.concurrent.CompletableFuture
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.exceptions.ExceptionManager
import org.readutf.hermes.listeners.ListenerManager
import org.readutf.hermes.platform.PacketPlatform
import java.util.function.Consumer
import org.readutf.hermes.response.ResponsePacket

class PacketManager<T : PacketPlatform>(
    val packetPlatform: T,
) {
    private val logger = KotlinLogging.logger { }
    private val listenerManager = ListenerManager()
    private val exceptionManager = ExceptionManager()
    val responseFutures = mutableMapOf<Int, CompletableFuture<ResponsePacket>>()

    init {
        packetPlatform.setupPacketListener { channel, packet ->
            onPacketReceived(channel, packet)
        }
        packetPlatform.init(this)
    }

    fun sendPacket(packet: Packet) {
        packetPlatform.sendPacket(packet)
    }

    inline fun <reified T> sendPacket(packet: Packet): CompletableFuture<T> {
        val future = CompletableFuture<ResponsePacket>()
        responseFutures[packet.packetId] = future
        packetPlatform.sendPacket(packet)
        return future.thenApply { responsePacket ->
            if (responsePacket is T) {
                responsePacket
            } else {
                throw IllegalStateException("Response packet was not of type ${T::class.java.simpleName}")
            }
        }
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
        fun <T : PacketPlatform> create(platform: T): PacketManager<T> = PacketManager(platform)
    }
}
