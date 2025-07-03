package org.readutf.hermes.codec;

import org.readutf.hermes.packet.Packet;

public interface PacketCodec {

    Packet<?> decode(byte[] data);

    byte[] encode(Packet<?> packet);

}
