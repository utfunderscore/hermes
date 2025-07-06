package org.readutf.hermes.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.jetbrains.annotations.NotNull;
import org.readutf.hermes.codec.PacketCodec;
import org.readutf.hermes.packet.Packet;

import java.util.List;

public class PacketDecoder extends ReplayingDecoder<Packet<?>> {

    private final @NotNull PacketCodec packetCodec;

    public PacketDecoder(@NotNull PacketCodec packetCodec) {
        this.packetCodec = packetCodec;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if(byteBuf.readableBytes() < 4) return;
        int length = byteBuf.readInt();
        if(byteBuf.readableBytes() < length) {
            byteBuf.readerIndex(byteBuf.readerIndex() - 4); // Reset the reader index to before the length
            return; // Not enough bytes to read the full packet
        }
        byte[] data = new byte[length];
        byteBuf.readBytes(data);
        Packet<?> packet = packetCodec.decode(data);

        list.add(packet);
    }
}
