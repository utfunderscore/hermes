package org.readutf.hermes.response

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel
import java.util.concurrent.CompletableFuture

public class ResponseManager {
    public val logger: KLogger = KotlinLogging.logger { }

    /**
     * A map of packet id to the future that will be completed when the response is received.
     */
    public val responseFutures: MutableMap<Int, CompletableFuture<ResponseDataPacket>> =
        mutableMapOf()

    internal fun sendPacketResponse(
        packet: Packet<*>,
        result: Result<Any?, Throwable>,
        hermesChannel: HermesChannel,
    ) {
        result
            .onSuccess { data ->
                hermesChannel.sendPacket(
                    ResponseDataPacket(
                        success = true,
                        response = data,
                        error = null,
                        originalId = packet.packetId,
                    ),
                )
            }.onFailure {
                logger.debug(it) { "Failed to handle packet" }
                hermesChannel.sendPacket(
                    ResponseDataPacket(
                        success = false,
                        response = null,
                        error = it.message,
                        originalId = packet.packetId,
                    ),
                )
            }
    }

    public fun getResponseFuture(packet: ResponseDataPacket): CompletableFuture<ResponseDataPacket>? =
        responseFutures.remove(packet.originalId)

    /**
     * Creates a future that can be completed when a response is received for the given packet.
     */
    public inline fun <reified T> getPacketResponseFuture(packet: Packet<T>): CompletableFuture<Result<T, Throwable>> {
        val future = CompletableFuture<ResponseDataPacket>()
        responseFutures[packet.packetId] = future

        return future.thenApply { responsePacket ->
            logger.debug { "Received response back for packet ${packet.packetId}" }

            if (responsePacket.success) {
                if (responsePacket.response is T) {
                    logger.debug { "Received back $responsePacket as ${T::class.java.simpleName}" }
                    return@thenApply Ok(responsePacket.response as T)
                } else {
                    logger.warn { "Response packet was not of type ${T::class.java.simpleName}" }
                    Err(IllegalStateException("Response packet was not of type ${T::class.java.simpleName}"))
                }
            } else {
                logger.warn { "Received error response: ${responsePacket.error}" }
                Err(IllegalStateException(responsePacket.error))
            }
        }
    }
}
