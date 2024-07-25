package org.readutf.hermes.channel

import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import org.readutf.hermes.response.ResponsePacket
import java.util.concurrent.CompletableFuture

abstract class HermesChannel(
    val channelId: String,
    val packetManager: PacketManager<*>,
) {
    abstract fun sendPacket(packet: Packet)

    inline fun <reified T> sendPacketFuture(packet: Packet): CompletableFuture<T> {
        val future = CompletableFuture<ResponsePacket>()

        println("storing ${packet.packetId}")

        packetManager.responseFutures[packet.packetId] = future
        sendPacket(packet)

        return future.thenApply {
            println("test $it")
            return@thenApply it as T
        }
    }
}
