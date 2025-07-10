package org.readutf.hermes.response;

import org.readutf.hermes.event.Listener;
import org.readutf.hermes.packet.Packet;
import org.readutf.hermes.packet.ResponsePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseManager {

    private static final Logger log = LoggerFactory.getLogger(ResponseManager.class);
    private final ConcurrentHashMap<Integer, ResponseData<?>> responseFutures = new ConcurrentHashMap<>();

    public <T> CompletableFuture<T> createFuture(Packet<T> packet, Class<? extends T> type) {
        if (!packet.expectsResponse()) {
            throw new IllegalArgumentException("Packet does not expect a response");
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        responseFutures.put(packet.getId(), new ResponseData<>((Class<T>) type, future));
        return future;
    }


    public Listener<ResponsePacket, Void> createResponseListener() {
        return (channel, packet) -> {

            log.debug("Received response packet: {}", packet);

            int originalPacketId = packet.getOriginalPacketId();
            ResponseData<?> responseData = responseFutures.remove(originalPacketId);

            if (responseData != null) {

                CompletableFuture<?> future = responseData.future;
                if(packet.isError()) {
                    future.completeExceptionally(new Exception("Error in response packet for original packet ID " + originalPacketId + ": " + packet.getResponseData()));
                    return null;
                }
                if (responseData.type.isInstance(packet.getResponseData())) {
                    responseData.complete(packet);
                } else {
                    future.completeExceptionally(new ClassCastException("Response data type mismatch"));
                }
            }
            return null;
        };
    }

    public record ResponseData<T>(Class<T> type, CompletableFuture<T> future) {

        public void complete(ResponsePacket responsePacket) {
            future.complete(type.cast(responsePacket.getResponseData()));
        }

    }


}
