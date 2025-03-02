package org.readutf.hermes.response

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.runCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener

/**
 * Listens for ResponseDataPacket, and completes the future associated with the packet.
 * @see org.readutf.hermes.PacketManager.sendPacketFuture
 */
public class ResponseDataListener(
    private val responseManager: ResponseManager,
) : TypedListener<ResponseDataPacket, HermesChannel, Unit> {
    private val logger = KotlinLogging.logger { }

    /**
     * Completes the future associated with the packet.
     * @param packet The packet to being handled.
     * @param channel The channel the packet was received on.
     * @return A result indicating success or failure.
     */
    override fun handle(
        packet: ResponseDataPacket,
        channel: HermesChannel,
    ): Result<Unit, Throwable> {
        logger.debug { "Received response packet ${packet.originalId}" }

        val future =
            responseManager.getResponseFuture(packet)
                ?: return Err(IllegalStateException("No future found for packet ${packet.originalId}"))

        return runCatching { future.complete(packet) }
            .onFailure {
                logger.debug(it) { "Failed to complete future for packet ${packet.originalId}" }
            }.map { }
    }
}
