package org.readutf.hermes;

import org.jetbrains.annotations.NotNull;
import org.readutf.hermes.codec.PacketCodec;
import org.readutf.hermes.event.Listener;
import org.readutf.hermes.event.PacketEventManager;
import org.readutf.hermes.packet.Packet;
import org.readutf.hermes.packet.ResponsePacket;
import org.readutf.hermes.platform.Channel;
import org.readutf.hermes.response.ResponseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public abstract class Hermes {

    private static final Logger log = LoggerFactory.getLogger(Hermes.class);
    private final @NotNull PacketCodec codec;
    private final @NotNull PacketEventManager eventManager;
    private final @NotNull ResponseManager responseManager;

    public Hermes(@NotNull PacketCodec codec) {
        this.codec = codec;
        this.eventManager = new PacketEventManager();
        this.responseManager = new ResponseManager();
        eventManager.listen(ResponsePacket.class, responseManager.createResponseListener());
    }

    public void sendPacket(Channel channel, Packet<?> packet) {
        byte[] packetData = codec.encode(packet);
        if (packetData == null || packetData.length == 0) {
            throw new IllegalArgumentException("Packet data cannot be null or empty");
        }

        if (channel == null) {
            throw new IllegalArgumentException("Packet channel cannot be null");
        }

        writeData(channel, packetData);
    }

    public void sendPacket(Packet<?> packet) throws Exception {
        byte[] packetData = codec.encode(packet);
        if (packetData == null || packetData.length == 0) {
            throw new IllegalArgumentException("Packet data cannot be null or empty");
        }

        writeData(packetData);
    }

    public <T> CompletableFuture<T> sendResponsePacket(Packet<T> packet, Class<? extends T> type) throws Exception {
        sendPacket(packet);
        return responseManager.createFuture(packet, type);
    }

    public <T> void listen(Class<? extends Packet<T>> type, @NotNull Listener<Packet<T>, T> listener) {
        eventManager.listen(type, listener);
    }

    protected abstract void start(InetSocketAddress address) throws Exception;

    /**
     * Sends a packet to the target channel.
     * The child class handles serialization of the packet.
     *
     * @param channel
     * @param packetData
     */
    protected abstract void writeData(Channel channel, byte[] packetData);

    /**
     * Sends a packet to the target channel.
     * The child class handles serialization of the packet.
     * warning: The method may not be implemented in all child classes.
     *
     * @param packetData
     */
    protected void writeData(byte[] packetData) throws Exception {
    }

    protected void readData(@NotNull Channel channel, byte[] packetData) {
        log.debug("Reading data from channel: {}", channel.getId());

        try {
            Packet<?> packet = codec.decode(packetData);
            Object result = eventManager.handlePacket(channel, packet);

            log.info("Received packet: {} from channel: {}", packet.getClass().getSimpleName(), channel.getId());

            if(packet.expectsResponse()) {
                sendPacket(channel, new ResponsePacket(packet.getId(), result));
            }
        } catch (Exception e) {
            log.error("Failed to decode packet from channel {}: {}", channel.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to decode packet", e);
        }
    }

}
