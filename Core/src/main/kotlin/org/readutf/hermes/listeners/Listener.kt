package org.readutf.hermes.listeners

import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel

interface Listener {
    fun acceptPacket(
        hermesChannel: HermesChannel,
        packet: Packet,
    ): Any?
}
