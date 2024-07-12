package org.readutf.hermes.channel

import org.readutf.hermes.Packet

class ChannelUnregisterPacket<T : HermesChannel>(
    val channel: T,
) : Packet
