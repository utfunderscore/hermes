package org.readutf.hermes.serializer

import com.esotericsoftware.kryo.kryo5.Kryo
import com.esotericsoftware.kryo.kryo5.io.Input
import com.esotericsoftware.kryo.kryo5.io.Output
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.Packet
import org.readutf.hermes.utils.Result
import java.io.ByteArrayOutputStream

class KryoPacketSerializer(
    private val kryo: Kryo,
) : PacketSerializer {
    private val logger = KotlinLogging.logger { }

    override fun serialize(packet: Packet): Result<ByteArray> {
        return try {
            Result.ok(
                ByteArrayOutputStream().use { outputStream ->
                    Output(outputStream).use { output ->
                        kryo.writeClassAndObject(output, packet)
                    }

                    logger.info { outputStream.toByteArray().contentToString() }
                    return@use outputStream.toByteArray()
                },
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to serialize packet" }
            Result.error(e.message ?: "Failed to serialize packet")
        }
    }

    override fun deserialize(bytes: ByteArray): Result<Packet> =
        try {
            logger.info { bytes.contentToString() }
            Result.ok(kryo.readClassAndObject(Input(bytes)) as Packet)
        } catch (e: Exception) {
            logger.error(e) { "Failed to deserialize packet" }
            Result.error(e.message ?: "Failed to deserialize packet")
        }
}
