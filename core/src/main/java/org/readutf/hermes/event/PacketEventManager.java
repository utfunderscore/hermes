package org.readutf.hermes.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.hermes.platform.Channel;
import org.readutf.hermes.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PacketEventManager {

    private static final Logger log = LoggerFactory.getLogger(PacketEventManager.class);
    private final ConcurrentHashMap<Class<?>, ConcurrentLinkedQueue<PrivateListener>> packetListeners = new ConcurrentHashMap<>();

    public <T> void listen(Class<? extends Packet<T>> type, Listener<? extends Packet<T>, T> listener) {
        ConcurrentLinkedQueue<PrivateListener> listeners = packetListeners.getOrDefault(type, new ConcurrentLinkedQueue<>());
        Listener<Packet<T>, T> castedListener = (Listener<Packet<T>, T>) listener;

        listeners.add((channel, packet) -> castedListener.onPacket(channel, type.cast(packet)));
        packetListeners.put(type, listeners);
    }

    public @Nullable Object handlePacket(Channel channel, Packet<?> packet) throws Exception {
        ConcurrentLinkedQueue<PrivateListener> listeners = packetListeners.get(packet.getClass());

        if (listeners.isEmpty()) {
            log.warn("No listeners registered for packet type: {}", packet.getClass().getName());
            return null;
        }

        @Nullable Object result = null;
        for (PrivateListener listener : listeners) {
            result = listener.onPacket(channel, packet);
        }
        return result;
    }

    private interface PrivateListener {

        @Nullable Object onPacket(@NotNull Channel channel, @NotNull Packet<?> packet) throws Exception;

    }

}
