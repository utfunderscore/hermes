package org.readutf.hermes.channel

import org.readutf.hermes.Packet

class ChannelUnregisterPacket<T : Channel>(
    val channel: T,
) : Packet
