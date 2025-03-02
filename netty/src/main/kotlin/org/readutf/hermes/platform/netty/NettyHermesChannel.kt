package org.readutf.hermes.platform.netty

import io.netty.channel.Channel
import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.HermesChannel

class NettyHermesChannel(
    private var channel: Channel,
    packetManager: PacketManager<NettyPlatform>,
) : HermesChannel(channelId = channel.id().asLongText(), packetManager) {
    override fun sendPacket(packet: Packet<*>) {
        logger.debug { "Sending packet: $packet" }
        channel.writeAndFlush(packet)
    }

    override fun close() {
        channel.flush()
        channel.close()
    }
}
