package org.readutf.hermes.listeners

import org.readutf.hermes.Packet

interface Listener {
    fun acceptPacket(packet: Packet)
}
