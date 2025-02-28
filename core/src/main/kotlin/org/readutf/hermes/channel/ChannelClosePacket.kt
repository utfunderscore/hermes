package org.readutf.hermes.channel

import org.readutf.hermes.Packet

class ChannelClosePacket<T : HermesChannel>(
    val channel: T,
) : Packet<Unit>()
