package org.readutf.hermes.platform.netty

import io.netty.channel.Channel
import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel

class NettyHermesChannel(
    var channel: Channel,
) : HermesChannel(channelId = channel.id().asLongText()) {
    override fun sendPacket(packet: Packet) {
        channel.writeAndFlush(packet)
    }
}
