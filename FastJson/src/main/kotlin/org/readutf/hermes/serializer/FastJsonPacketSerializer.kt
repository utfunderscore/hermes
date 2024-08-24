package org.readutf.hermes.serializer

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import org.readutf.hermes.Packet
import org.readutf.hermes.utils.Result

class FastJsonPacketSerializer : PacketSerializer {
    override fun serialize(packet: Packet): Result<ByteArray> =
        try {
            Result.ok(
                JSON.toJSONBytes(
                    mapOf(
                        "class" to packet::class.java.name,
                        "data" to packet,
                    ),
                ),
            )
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to serialize packet")
        }

    override fun deserialize(bytes: ByteArray): Result<Packet> {
        val jsonObject: JSONObject = JSON.parseObject(bytes)

        val className = jsonObject.getString("class")
        val data = jsonObject.getJSONObject("data")

        return try {
            Result.ok(JSON.parseObject(data.toString(), Class.forName(className)) as Packet)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to deserialize packet")
        }
    }
}
