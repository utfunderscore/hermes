package org.readutf.hermes.channel

import org.readutf.hermes.Packet

class ChannelRegisterPacket<T : HermesChannel>(
    val channel: T,
) : Packet
