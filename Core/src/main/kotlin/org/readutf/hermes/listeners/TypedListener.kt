package org.readutf.hermes.listeners

import org.readutf.hermes.Packet

class TypedListener<T : Packet> {
    fun handle(packet: T) {
        println("Received packet: $packet")
    }
}
