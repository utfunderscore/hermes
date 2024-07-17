package org.readutf.hermes.channel

import org.readutf.hermes.Packet

class ChannelOpenPacket<T : HermesChannel>(
    val channel: T,
) : Packet
