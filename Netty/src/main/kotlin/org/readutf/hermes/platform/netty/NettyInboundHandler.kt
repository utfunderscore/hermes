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
        ctx: ChannelHandlerContext,
        msg: Any?,
    ) {
        logger.info { "Received" }

        if (msg !is Packet) {
            logger.info { "Received unknown message: $msg" }
            return
        }

        val hermesChannel = packetPlatform.getChannel(ctx.channel())

        packetPlatform.handlePacket(hermesChannel, msg)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info { "New connection from ${ctx.channel().remoteAddress()}" }
        packetPlatform.activeChannels.add(ctx.channel())

        val hermesChannel = packetPlatform.getChannel(ctx.channel())

        packetPlatform.handlePacket(hermesChannel, ChannelRegisterPacket(hermesChannel))
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info { "Connection closed from ${ctx.channel().remoteAddress()}" }
        packetPlatform.activeChannels.remove(ctx.channel())

        val hermesChannel = packetPlatform.getChannel(ctx.channel())

        packetPlatform.handlePacket(hermesChannel, ChannelUnregisterPacket(hermesChannel))

        packetPlatform.removeChannel(ctx.channel())
    }

    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        logger.info { "Exception caught" }

        packetPlatform.packetManager.handleException(cause)
    }
}
