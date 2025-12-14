package org.readutf.hermes.packet;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResponsePacket implements Packet<Void> {

    private final int originalPacketId;
    private final @Nullable Object responseData;
    private final boolean error;

    private ResponsePacket(int originalPacketId, @Nullable Object responseData, boolean error) {
        this.originalPacketId = originalPacketId;
        this.responseData = responseData;
        this.error = error;
    }

    public int getOriginalPacketId() {
        return originalPacketId;
    }

    public @Nullable Object getResponseData() {
        return responseData;
    }

    public boolean isError() {
        return error;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull ResponsePacket error(int originalPacketId, @Nullable String errorMessage) {
        return new ResponsePacket(originalPacketId, errorMessage, true);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull ResponsePacket success(int originalPacketId, @Nullable Object responseData) {
        return new ResponsePacket(originalPacketId, responseData, false);
    }

    @Override
    public boolean expectsResponse() {
        return false;
    }

    @Override
    public int getId() {
        return Integer.MIN_VALUE + 1;
    }
}
