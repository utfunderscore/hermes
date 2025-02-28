package org.readutf.hermes.response

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener

class ResponseDataListener(
    private val packetManager: PacketManager<*>,
) : TypedListener<ResponseDataPacket, HermesChannel, Unit> {
    private val logger = KotlinLogging.logger { }

    override fun handle(
        packet: ResponseDataPacket,
        channel: HermesChannel,
    ): Result<Unit, Throwable> {
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

        return Ok(Unit)
    }
}
