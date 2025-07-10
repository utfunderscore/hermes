package org.readutf.hermes.packet;

import org.jetbrains.annotations.Nullable;

public class ResponsePacket extends Packet<Void> {

    private final int originalPacketId;
    private final @Nullable Object responseData;
    private final boolean error;

    private ResponsePacket(int originalPacketId, @Nullable Object responseData, boolean error) {
        super(false);
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

    public static ResponsePacket error(int originalPacketId, @Nullable String errorMessage) {
        return new ResponsePacket(originalPacketId, errorMessage, true);
    }

    public static ResponsePacket success(int originalPacketId, @Nullable Object responseData) {
        return new ResponsePacket(originalPacketId, responseData, false);
    }

}
