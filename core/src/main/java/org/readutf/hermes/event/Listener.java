package org.readutf.hermes.event;

import org.readutf.hermes.platform.Channel;
import org.readutf.hermes.packet.Packet;

public interface Listener<PACKET extends Packet<RESPONSE>, RESPONSE> {

    RESPONSE onPacket(Channel channel, PACKET event) throws Exception;

}
