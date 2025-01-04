package org.readutf.hermes.serializer

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.Packet
import org.readutf.hermes.utils.Result

class JacksonPacketSerializer(
    private val objectMapper: ObjectMapper,
) : PacketSerializer {
    private val logger = KotlinLogging.logger { }

    override fun deserialize(bytes: ByteArray): Result<Packet> {
        val jsonString = String(bytes)

        return try {
            val packetWrapper = objectMapper.readValue(jsonString, object : TypeReference<TypedPacketWrapper>() {})
            logger.info { "Received ${packetWrapper.packet}" }
            Result.ok(packetWrapper.packet)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to deserialize packet")
        }
    }

    override fun serialize(packet: Packet): Result<ByteArray> {
        val packetWrapper = TypedPacketWrapper(packet)

        return try {
            val json = objectMapper.writeValueAsString(packetWrapper)
            logger.info { "Sending $packet" }
            Result.ok(json.toByteArray())
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to serialize packet")
        }
    }

    /**
     * Wrapper object to include type information in the serialized JSON.
     */
    class TypedPacketWrapper(
        @field:JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CLASS) val packet: Packet,
    )
}
