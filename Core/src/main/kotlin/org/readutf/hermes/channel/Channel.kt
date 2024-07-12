package org.readutf.hermes.channel

import org.readutf.hermes.Packet

abstract class Channel(
    val channelId: String,
) {
    abstract fun sendPacket(packet: Packet)
}
