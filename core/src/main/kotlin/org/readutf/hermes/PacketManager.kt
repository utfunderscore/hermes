package org.readutf.hermes

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.exceptions.ExceptionManager
import org.readutf.hermes.listeners.ListenerManager
import org.readutf.hermes.platform.PacketPlatform
import org.readutf.hermes.response.ResponseDataListener
import org.readutf.hermes.response.ResponseDataPacket
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
    val responseFutures = mutableMapOf<Int, CompletableFuture<ResponseDataPacket>>()

    init {
        packetPlatform.setupPacketListener { channel, packet ->
            onPacketReceived(channel, packet)
        }
        packetPlatform.init(this)
        editListeners {
            it.registerListener(ResponseDataListener(this))
        }
    }

    fun sendPacket(packet: Packet<*>) {
        packetPlatform.sendPacket(packet)
    }

    inline fun <reified T> sendPacket(packet: Packet<T>): CompletableFuture<T> {
        val future = CompletableFuture<ResponseDataPacket>()
        responseFutures[packet.packetId] = future
        packetPlatform.sendPacket(packet)

        return future.thenApply { responsePacket ->
            logger.debug { "Received response back for packet ${packet.packetId}" }

            if (responsePacket.response is T) {
                logger.debug { "Received back $responsePacket as ${T::class.java.simpleName}" }
                return@thenApply responsePacket.response
            } else {
                logger.warn { "Response packet was not of type ${T::class.java.simpleName}" }
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

    fun editListeners(consumer: (ListenerManager) -> Unit): PacketManager<T> {
        consumer(listenerManager)
        return this
    }

    private fun onPacketReceived(
        hermesChannel: HermesChannel,
        packet: Packet<*>,
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
