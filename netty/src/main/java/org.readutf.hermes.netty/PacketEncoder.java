package org.readutf.hermes.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.jetbrains.annotations.NotNull;
import org.readutf.hermes.codec.PacketCodec;
import org.readutf.hermes.packet.Packet;

public class PacketEncoder extends MessageToByteEncoder<Packet<?>> {

    private final @NotNull PacketCodec codec;

    public PacketEncoder(PacketCodec codec) {
        this.codec = codec;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<?> msg, ByteBuf out) throws Exception {
        byte[] data = codec.encode(msg);
        if (data == null) {
            throw new IllegalArgumentException("Packet encoding returned null for packet: " + msg);
        }

        if (data.length > Integer.MAX_VALUE - 4) {
            throw new IllegalArgumentException("Encoded packet size exceeds maximum allowed size: " + data.length);
        }

        out.writeInt(data.length); // Write the length of the packet
        out.writeBytes(data); // Write the actual packet data
    }
}
