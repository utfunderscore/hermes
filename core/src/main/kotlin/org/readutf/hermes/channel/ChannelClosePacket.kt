package org.readutf.hermes.channel

import org.readutf.hermes.Packet

public class ChannelClosePacket<T : HermesChannel>(
    public val channel: T,
) : Packet<Unit>()
