package org.readutf.hermes.channel

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import org.readutf.hermes.response.ResponsePacket
import java.util.concurrent.CompletableFuture

abstract class HermesChannel(
    val channelId: String,
    val packetManager: PacketManager<*>,
) {
    val logger = KotlinLogging.logger {}

    abstract fun sendPacket(packet: Packet)

    abstract fun close()

    inline fun <reified T> sendPacketFuture(packet: Packet): CompletableFuture<T> {
        val storedFuture = CompletableFuture<ResponsePacket>()

        logger.debug { "storing ${packet.packetId}" }

        packetManager.responseFutures[packet.packetId] = storedFuture
        sendPacket(packet)

        var packetFuture = CompletableFuture<T>()

        return storedFuture.thenApplyAsync({
            try {
                logger.debug { "Completing future with ${it.response.javaClass.simpleName} as ${T::class.java.simpleName}" }

                return@thenApplyAsync it.response as T
            } catch (e: Exception) {
                logger.error { "Failed to cast response to ${T::class.java.simpleName}" }
                throw e
            }
        }, packetManager.executorService)
    }
}
