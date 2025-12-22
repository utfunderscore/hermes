package org.readutf.hermes;

import org.jetbrains.annotations.NotNull;
import org.readutf.hermes.codec.PacketCodec;
import org.readutf.hermes.event.Listener;
import org.readutf.hermes.event.PacketEventManager;
import org.readutf.hermes.packet.Packet;
import org.readutf.hermes.packet.ResponsePacket;
import org.readutf.hermes.platform.HermesChannel;
import org.readutf.hermes.response.ResponseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

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

    public void sendPacket(HermesChannel hermesChannel, Packet<?> packet) {
        byte[] packetData = codec.encode(packet);
        if (packetData == null || packetData.length == 0) {
            throw new IllegalArgumentException("Packet data cannot be null or empty");
        }

        if (hermesChannel == null) {
            throw new IllegalArgumentException("Packet channel cannot be null");
        }

        log.debug("Sending packet: {} to channel: {}", packet.getClass().getSimpleName(), hermesChannel.getId());
        writeData(hermesChannel, packetData);
    }

    public void sendPacket(Packet<?> packet) throws Exception {
        byte[] packetData = codec.encode(packet);
        if (packetData == null || packetData.length == 0) {
            throw new IllegalArgumentException("Packet data cannot be null or empty");
        }

        writeData(packetData);
    }

    public <T> CompletableFuture<T> sendResponsePacket(Packet<T> packet, Class<? extends T> type) {
        try {
            sendPacket(packet);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(null);
        }
        return responseManager.createFuture(packet, type);
    }

    public <T> CompletableFuture<T> sendResponsePacket(HermesChannel channel, Packet<T> packet, Class<? extends T> type) {
        try {
            sendPacket(channel, packet);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(null);
        }
        return responseManager.createFuture(packet, type);
    }

    public <T, V extends Packet<T>> void listen(Class<V> type, @NotNull Listener<V, T> listener) {
        eventManager.listen(type, listener);
    }

    public <T extends Packet<Void>> void listenIgnore(Class<T> type, @NotNull BiConsumer<HermesChannel, T> listener) {
       listen(type, (channel, event) -> {
           listener.accept(channel, event);
           return null;
       });
    }

    protected abstract void start(InetSocketAddress address) throws Exception;

    /**
     * Sends a packet to the target channel.
     * The child class handles serialization of the packet.
     *
     * @param hermesChannel
     * @param packetData
     */
    protected abstract void writeData(HermesChannel hermesChannel, byte[] packetData);

    /**
     * Sends a packet to the target channel.
     * The child class handles serialization of the packet.
     * warning: The method may not be implemented in all child classes.
     *
     * @param packetData
     */
    protected void writeData(byte[] packetData) throws Exception {
    }

    protected void readData(@NotNull HermesChannel hermesChannel, byte[] packetData) {
        log.debug("Reading data from channel: {}", hermesChannel.getId());
        Packet<?> packet = codec.decode(packetData);

        handlePacket(hermesChannel, packet);
    }

    protected void handlePacket(HermesChannel hermesChannel, Packet<?> packet) {
        log.debug("Handling packet: {} from channel: {}", packet.getClass().getSimpleName(), hermesChannel.getId());

        try {
            Object result = eventManager.handlePacket(hermesChannel, packet);

            log.debug("Received packet: {} from channel: {}", packet.getClass().getSimpleName(), hermesChannel.getId());

            if (packet.expectsResponse()) {
                sendPacket(hermesChannel, ResponsePacket.success(packet.getId(), result));
            }
        } catch (Exception e) {
            log.error("Failed to handle packet: {}", packet.getClass().getSimpleName(), e);
            if (packet.expectsResponse()) {
                sendPacket(hermesChannel, ResponsePacket.error(packet.getId(), e.getMessage()));
            }
        }

    }

    public @NotNull PacketCodec getCodec() {
        return codec;
    }
}
