package org.readutf.hermes.platform.netty

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.readutf.hermes.Packet
import org.readutf.hermes.channel.ChannelClosePacket
import org.readutf.hermes.channel.ChannelOpenPacket

class NettyInboundHandler(
    private val packetPlatform: NettyPlatform,
) : ChannelInboundHandlerAdapter() {
    private val logger = KotlinLogging.logger { }

    override fun channelRead(
        ctx: ChannelHandlerContext,
        msg: Any?,
    ) {
        if (msg !is Packet) {
            logger.warn { "Received unknown message: $msg" }
            return
        }

        val hermesChannel = packetPlatform.getChannel(ctx.channel())

        packetPlatform.handlePacket(hermesChannel, msg)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info { "New connection from ${ctx.channel().remoteAddress()}" }
        packetPlatform.activeChannels[ctx.channel().id().asLongText()] = ctx.channel()

        val hermesChannel = packetPlatform.getChannel(ctx.channel())

        packetPlatform.handlePacket(hermesChannel, ChannelOpenPacket(hermesChannel))
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info { "Connection closed from ${ctx.channel().remoteAddress()}" }
        packetPlatform.activeChannels.remove(ctx.channel().id().asLongText())

        val hermesChannel = packetPlatform.getChannel(ctx.channel())

        packetPlatform.handlePacket(hermesChannel, ChannelClosePacket(hermesChannel))

        packetPlatform.removeChannel(ctx.channel())
    }

    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        val handled = packetPlatform.packetManager.handleException(cause)
        if (!handled) {
            throw cause
        }
    }
}
