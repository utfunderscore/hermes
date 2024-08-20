package org.readutf.hermes.serializer

import org.readutf.hermes.Packet
import org.readutf.hermes.utils.Result

interface PacketSerializer {
    fun serialize(packet: Packet): Result<ByteArray>

    fun deserialize(bytes: ByteArray): Result<Packet>
}
