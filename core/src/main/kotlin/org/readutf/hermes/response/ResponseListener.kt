package org.readutf.hermes.response

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener

class ResponseListener(
    private val packetManager: PacketManager<*>,
) : TypedListener<ResponsePacket, HermesChannel, Unit> {
    private val logger = KotlinLogging.logger { }

    override fun handle(
        packet: ResponsePacket,
        channel: HermesChannel,
    ) {
        logger.debug { "Received response packet ${packet.originalId}" }

        val future = packetManager.responseFutures[packet.originalId]

        if (future != null) {
            try {
                logger.debug { "Completing future with ${packet.response}" }
                future.complete(packet)
            } catch (exception: Exception) {
                logger.error(exception) { "Error completing future" }
            }
        } else {
            logger.debug { "No future found" }
        }
    }
}
