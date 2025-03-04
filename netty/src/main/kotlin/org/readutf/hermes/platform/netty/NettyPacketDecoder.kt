package org.readutf.hermes.platform.netty

import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import org.readutf.hermes.Packet
import org.readutf.hermes.serializer.PacketSerializer

class NettyPacketDecoder(
    private val packetSerializer: PacketSerializer,
) : ReplayingDecoder<Packet<*>>() {
    private val logger = KotlinLogging.logger { }

    override fun decode(
        contex: ChannelHandlerContext?,
        buffer: ByteBuf,
        packets: MutableList<Any>,
    ) {
        if (buffer.readableBytes() < 4) {
            return
        }

        val length = buffer.readInt()
        if (buffer.readableBytes() < length) {
            buffer.resetReaderIndex()
            return
        }

        val bytes = ByteArray(length)
        buffer.readBytes(bytes)

        val packetResult =
            packetSerializer.deserialize(bytes).getOrElse {
                logger.warn { "Failed to deserialize packet: $it" }
                return
            }

        packets.add(packetResult)
    }
}
