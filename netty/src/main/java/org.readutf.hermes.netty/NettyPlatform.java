package org.readutf.hermes.netty;

import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.hermes.Hermes;
import org.readutf.hermes.codec.PacketCodec;
import org.readutf.hermes.packet.ChannelClosePacket;
import org.readutf.hermes.packet.Packet;
import org.readutf.hermes.platform.HermesChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NettyPlatform extends Hermes {

    private static final Logger log = LoggerFactory.getLogger(NettyPlatform.class);
    protected @Nullable Thread thread;

    protected final @NotNull Map<HermesChannel, Channel> hermesToNettyChannel;
    protected final @NotNull Map<Channel, HermesChannel> nettyToHermesChannel;

    protected NettyPlatform(@NotNull PacketCodec codec) {
        super(codec);
        this.hermesToNettyChannel = new ConcurrentHashMap<>();
        this.nettyToHermesChannel = new ConcurrentHashMap<>();
    }

    @Override
    public void handlePacket(HermesChannel hermesChannel, Packet<?> packet) {
        super.handlePacket(hermesChannel, packet);
    }

    @Override
    public void sendPacket(HermesChannel hermesChannel, Packet<?> packet) {
        Channel nettyChannel = hermesToNettyChannel.get(hermesChannel);
        if (nettyChannel != null) {
            nettyChannel.writeAndFlush(packet).addListener(future -> {
                if (!future.isSuccess()) {
                    log.error("Failed to send packet to channel {}: {}", hermesChannel.getId(), future.cause().getMessage());
                }
            });
        } else {
            log.warn("Attempted to send packet to a channel that is not registered: {}", hermesChannel.getId());
        }
    }

    @Override
    protected void writeData(HermesChannel hermesChannel, byte[] packetData) {
        throw new UnsupportedOperationException("Netty platform does not support writing data directly");
    }

    @Override
    protected void writeData(byte[] packetData) throws Exception {
        throw new UnsupportedOperationException("Netty platform does not support writing data directly");
    }

    public @Nullable Channel getNettyChannel(HermesChannel hermesChannel) {
        return hermesToNettyChannel.get(hermesChannel);
    }

    public void unregisterChannel(Channel nettyChannel) {
        HermesChannel hermesChannel = nettyToHermesChannel.remove(nettyChannel);
        handlePacket(hermesChannel, new ChannelClosePacket());
        if (hermesChannel != null) {
            hermesToNettyChannel.remove(hermesChannel);
            log.info("Unregistered channel: {}", hermesChannel.getId());
        } else {
            log.warn("Attempted to unregister a channel that was not found: {}", nettyChannel.id());
        }
    }

    public @Nullable HermesChannel getHermesChannel(Channel nettyChannel) {
        return nettyToHermesChannel.get(nettyChannel);
    }

    protected abstract void initializeBootstrap();

    /**
     * Registers a connection between a Hermes channel and a Netty channel
     */
    protected void registerChannel(HermesChannel hermesChannel, Channel nettyChannel) {
        hermesToNettyChannel.put(hermesChannel, nettyChannel);
        nettyToHermesChannel.put(nettyChannel, hermesChannel);
        log.info("Registered new channel: {}", hermesChannel.getId());
    }

    /**
     * Cleanly shuts down the event loop groups
     */
    public abstract void shutdown();
}