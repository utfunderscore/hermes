package org.readutf.hermes.response

import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener

class ResponseListener(
    val packetManager: PacketManager<*>,
) : TypedListener<ResponsePacket, HermesChannel> {
    override fun handle(
        packet: ResponsePacket,
        channel: HermesChannel,
    ) {
        val responseFutures = packetManager.responseFutures

        responseFutures[packet.packetId]?.complete(packet)
    }
}
