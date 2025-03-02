package org.readutf.hermes.channel

import org.readutf.hermes.Packet

public class ChannelOpenPacket<T : HermesChannel>(
    public val channel: T,
) : Packet<Unit>()
