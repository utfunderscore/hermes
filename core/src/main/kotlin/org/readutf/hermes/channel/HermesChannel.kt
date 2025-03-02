package org.readutf.hermes.channel

import com.github.michaelbull.result.Result
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import java.util.concurrent.CompletableFuture

public abstract class HermesChannel(
    public val channelId: String,
    public val packetManager: PacketManager<*>,
) {
    public val logger: KLogger = KotlinLogging.logger {}

    public abstract fun sendPacket(packet: Packet<*>)

    public abstract fun close()

    public inline fun <reified T> sendPacketFuture(packet: Packet<T>): CompletableFuture<Result<T, Throwable>> {
        sendPacket(packet)

        return packetManager.responseManager.getPacketResponseFuture(packet)
    }
}
