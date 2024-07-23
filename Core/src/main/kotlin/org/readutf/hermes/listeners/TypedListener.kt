package org.readutf.hermes.listeners

import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel

interface TypedListener<T : Packet, U : HermesChannel> {
    fun handle(
        packet: T,
        channel: U,
    ): Any
}
