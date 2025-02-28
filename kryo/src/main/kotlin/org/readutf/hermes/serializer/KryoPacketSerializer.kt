package org.readutf.hermes.serializer

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.util.Pool
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.Packet
import java.io.ByteArrayOutputStream

class KryoPacketSerializer(
    private val kryoPool: Pool<Kryo>,
) : PacketSerializer {
    private val logger = KotlinLogging.logger { }

    override fun serialize(packet: Packet<*>): Result<ByteArray, Throwable> {
        return try {
            Ok(
                ByteArrayOutputStream().use { outputStream ->
                    Output(outputStream).use { output ->
                        val kryo = kryoPool.obtain()
                        kryo.writeClassAndObject(output, packet)
                        kryoPool.free(kryo)
                    }
                    return@use outputStream.toByteArray()
                },
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to serialize packet" }
            Err(e)
        }
    }

    override fun deserialize(bytes: ByteArray): Result<Packet<*>, Throwable> {
        synchronized(kryoPool) {
            try {
                val kryo = kryoPool.obtain()
                val packet = kryo.readClassAndObject(Input(bytes)) as Packet<*>
                kryoPool.free(kryo)
                return Ok(packet)
            } catch (e: Exception) {
                logger.error(e) { "Failed to deserialize packet array = ${bytes.contentToString()}" }
                return Err(e)
            }
        }
    }
}
