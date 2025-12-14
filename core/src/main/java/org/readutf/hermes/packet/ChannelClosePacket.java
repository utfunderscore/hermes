package org.readutf.hermes.packet;

public class ChannelClosePacket implements Packet<Void> {

    @Override
    public boolean expectsResponse() {
        return false;
    }

    @Override
    public int getId() {
        return Integer.MIN_VALUE;
    }
}
