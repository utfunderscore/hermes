package org.readutf.hermes.event;

import org.readutf.hermes.platform.HermesChannel;
import org.readutf.hermes.packet.Packet;

public interface Listener<PACKET extends Packet<RESPONSE>, RESPONSE> {

    RESPONSE onPacket(HermesChannel hermesChannel, PACKET event) throws Exception;

}
