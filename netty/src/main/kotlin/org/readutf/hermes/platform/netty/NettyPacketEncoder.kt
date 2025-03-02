package org.readutf.hermes.platform.netty

import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.readutf.hermes.Packet
import org.readutf.hermes.metrics.HermesMetrics
import org.readutf.hermes.serializer.PacketSerializer

class NettyPacketEncoder(
    private val packetSerializer: PacketSerializer,
) : MessageToByteEncoder<Packet<*>>() {
    override fun encode(
        context: ChannelHandlerContext,
        packet: Packet<*>,
        byteBuf: ByteBuf,
    ) {
        val serializedResult = packetSerializer.serialize(packet)
        val logger = KotlinLogging.logger { }

        val byteArray =
            serializedResult.getOrElse { err ->
                logger.error(err) { "Failed to serialize packet" }
                return
            }

        HermesMetrics.packetSize.record(byteArray.size.toDouble())

        byteArray.let {
            byteBuf.writeInt(it.size)
            byteBuf.writeBytes(it)
        }
    }
}
