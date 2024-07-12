package org.readutf.hermes.channel

import org.readutf.hermes.Packet

abstract class HermesChannel(
    val channelId: String,
) {
    abstract fun sendPacket(packet: Packet)
}
