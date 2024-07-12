package org.readutf.hermes.platform.netty

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.readutf.hermes.Packet
import org.readutf.hermes.channel.ChannelRegisterPacket
import org.readutf.hermes.channel.ChannelUnregisterPacket

class NettyInboundHandler(
    private val packetPlatform: NettyPlatform,
) : ChannelInboundHandlerAdapter() {
    private val logger = KotlinLogging.logger { }

    override fun channelRead(
        ctx: ChannelHandlerContext?,
        msg: Any?,
    ) {
        logger.info { "Received" }

        if (msg !is Packet) {
            logger.info { "Received unknown message: $msg" }
            return
        }

        packetPlatform.handlePacket(msg)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info { "New connection from ${ctx.channel().remoteAddress()}" }
        packetPlatform.activeChannels.add(ctx.channel())

        packetPlatform.handlePacket(ChannelRegisterPacket(NettyChannel(ctx.channel())))
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info { "Connection closed from ${ctx.channel().remoteAddress()}" }
        packetPlatform.activeChannels.remove(ctx.channel())

        packetPlatform.handlePacket(ChannelUnregisterPacket(NettyChannel(ctx.channel())))
    }
}
