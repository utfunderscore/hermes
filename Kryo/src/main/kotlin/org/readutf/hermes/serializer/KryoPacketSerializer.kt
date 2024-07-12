package org.readutf.hermes.serializer

import com.esotericsoftware.kryo.kryo5.Kryo
import com.esotericsoftware.kryo.kryo5.io.Input
import com.esotericsoftware.kryo.kryo5.io.Output
import panda.std.Result
import org.readutf.hermes.Packet
import java.io.ByteArrayOutputStream

class KryoPacketSerializer(
    private val kryo: Kryo,
) : PacketSerializer {
    override fun serialize(packet: Packet): Result<ByteArray, String> {
        return try {
            Result.ok(
                ByteArrayOutputStream().use { outputStream ->
                    Output(outputStream).use { output ->
                        kryo.writeClassAndObject(output, packet)
                    }
                    return@use outputStream.toByteArray()
                },
            )
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to serialize packet")
        }
    }

    override fun deserialize(bytes: ByteArray): Result<Packet, String> =
        try {
            Result.ok(kryo.readClassAndObject(Input(bytes)) as Packet)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to deserialize packet")
        }
}
