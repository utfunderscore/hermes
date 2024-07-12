package org.readutf.hermes.platform.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import org.readutf.hermes.serializer.PacketSerializer
import org.readutf.hermes.Packet

class NettyPacketDecoder(
    private val packetSerializer: PacketSerializer,
) : ReplayingDecoder<Packet>() {
    override fun decode(
        contex: ChannelHandlerContext?,
        buffer: ByteBuf,
        packets: MutableList<Any>,
    ) {
        buffer.resetReaderIndex()

        if (buffer.readableBytes() < 2) {
            return
        }

        val length = buffer.readInt()
        if (buffer.readableBytes() < length) {
            return
        }

        val bytes = ByteArray(length)
        buffer.readBytes(bytes)

        val packetResult = packetSerializer.deserialize(bytes)
        if (packetResult.isErr) {
            println("Failed to deserialize packet: ${packetResult.error}")
            return
        }

        packets.add(packetResult.get())
    }
}
