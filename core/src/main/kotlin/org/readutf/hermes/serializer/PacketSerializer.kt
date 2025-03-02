package org.readutf.hermes.serializer

import com.github.michaelbull.result.Result
import org.readutf.hermes.Packet

public interface PacketSerializer {
    public fun serialize(packet: Packet<*>): Result<ByteArray, Throwable>

    public fun deserialize(bytes: ByteArray): Result<Packet<*>, Throwable>
}
