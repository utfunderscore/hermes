package org.readutf.hermes.serializer

import org.readutf.hermes.Packet
import panda.std.Result

interface PacketSerializer {
    fun serialize(packet: Packet): Result<ByteArray, String>

    fun deserialize(bytes: ByteArray): Result<Packet, String>
}
