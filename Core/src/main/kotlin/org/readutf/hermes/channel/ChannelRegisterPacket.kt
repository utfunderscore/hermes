package org.readutf.hermes.channel

import org.readutf.hermes.Packet

class ChannelRegisterPacket<T : Channel>(
    val channel: T,
) : Packet
