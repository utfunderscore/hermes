package org.readutf.hermes.platform.netty

import io.netty.channel.Channel
import org.readutf.hermes.Packet

class NettyChannel(
    private var channel: Channel,
) : org.readutf.hermes.channel.Channel(channelId = channel.id().asLongText()) {
    override fun sendPacket(packet: Packet) {
        channel.writeAndFlush(packet)
    }
}
