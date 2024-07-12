package org.readutf.hermes.listeners

import org.readutf.hermes.Packet

interface TypedListener<T : Packet> {
    fun acceptPacket(packet: T)
}
