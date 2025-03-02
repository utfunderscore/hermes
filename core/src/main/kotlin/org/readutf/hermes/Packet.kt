package org.readutf.hermes

import java.util.concurrent.atomic.AtomicInteger

/**
 * All data sent between the client and server should extend this class.
 * @param packetId The id of the packet, useful when a response is needed.
 * @param RESPONSE The type of response expected from the packet.
 * @see org.readutf.hermes.response.ResponseDataPacket
 */
public abstract class Packet<RESPONSE>(
    public val packetId: Int = currentId.getAndIncrement(),
) {
    public companion object {
        public val currentId: AtomicInteger = AtomicInteger(0)
    }
}
