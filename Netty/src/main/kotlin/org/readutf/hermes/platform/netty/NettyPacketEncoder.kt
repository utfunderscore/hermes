package org.readutf.hermes.platform.netty

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.readutf.hermes.Packet
import org.readutf.hermes.serializer.PacketSerializer
import java.nio.ByteBuffer

class NettyPacketEncoder(
    private val packetSerializer: PacketSerializer,
) : MessageToByteEncoder<Packet>() {
    override fun encode(
        context: ChannelHandlerContext,
        packet: Packet,
        byteBuf: ByteBuf,
    ) {
        val serializedResult = packetSerializer.serialize(packet)
        val logger = KotlinLogging.logger { }

        if (serializedResult.isError()) {
            logger.error { "Failed to serialize packet: ${serializedResult.getError()}" }
            return
        }

        val serialized = serializedResult.get()

        val buffer = ByteBuffer.allocate(4 + serialized.size)

        serialized.let {
            byteBuf.writeInt(it.size)
            byteBuf.writeBytes(it)

            buffer.putInt(it.size)
            buffer.put(it)
        }
    }
}
