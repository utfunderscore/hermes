package org.readutf.hermes.serializer

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.Packet

class JacksonPacketSerializer(
    private val objectMapper: ObjectMapper,
) : PacketSerializer {
    private val logger = KotlinLogging.logger { }

    override fun deserialize(bytes: ByteArray): com.github.michaelbull.result.Result<Packet<*>, Throwable> {
        val jsonString = String(bytes)

        return try {
            val packetWrapper = objectMapper.readValue(jsonString, object : TypeReference<TypedPacketWrapper>() {})
            logger.info { "Received ${packetWrapper.packet}" }
            Ok(packetWrapper.packet)
        } catch (e: Exception) {
            Err(e)
        }
    }

    override fun serialize(packet: Packet<*>): com.github.michaelbull.result.Result<ByteArray, Throwable> {
        val packetWrapper = TypedPacketWrapper(packet)

        return try {
            val json = objectMapper.writeValueAsString(packetWrapper)
            logger.info { "Sending $packet" }
            Ok(json.toByteArray())
        } catch (e: Exception) {
            Err(e)
        }
    }

    /**
     * Wrapper object to include type information in the serialized JSON.
     */
    class TypedPacketWrapper(
        @field:JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CLASS) val packet: Packet<*>,
    )
}
