package org.readutf.hermes.packet;

import org.readutf.hermes.platform.Channel;

public class ChannelClosePacket extends Packet<Void> {
    public ChannelClosePacket(Channel channel) {
        super(false);
    }
}
