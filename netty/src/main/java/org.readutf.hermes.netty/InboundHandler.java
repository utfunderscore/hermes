package org.readutf.hermes.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.jetbrains.annotations.NotNull;
import org.readutf.hermes.packet.Packet;
import org.readutf.hermes.platform.HermesChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboundHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(InboundHandler.class);

    private final @NotNull NettyPlatform nettyPlatform;

    public InboundHandler(@NotNull NettyPlatform nettyPlatform) {
        this.nettyPlatform = nettyPlatform;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof Packet<?>)) {
            log.warn("Received non-packet object: {}", msg);
        }

        Packet<?> packet = (Packet<?>) msg;

        Channel channel = ctx.channel();
        HermesChannel hermesChannel = nettyPlatform.getHermesChannel(ctx.channel());
        nettyPlatform.handlePacket(hermesChannel, packet);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        nettyPlatform.registerChannel(new HermesChannel(), ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        nettyPlatform.unregisterChannel(ctx.channel());
    }
}
