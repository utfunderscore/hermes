package org.readutf.hermes.response

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener

class ResponseListener(
    private val packetManager: PacketManager<*>,
) : TypedListener<ResponsePacket, HermesChannel> {
    private val logger = KotlinLogging.logger { }

    override fun handle(
        packet: ResponsePacket,
        channel: HermesChannel,
    ) {
        logger.info { "Received response packet ${packet.originalId}" }

        val future = packetManager.responseFutures[packet.originalId]

        if (future != null) {
            println("Completing future with $packet")
            future.complete(packet)
        } else {
            println("No future found")
        }
    }
}
