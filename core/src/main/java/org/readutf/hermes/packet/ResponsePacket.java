package org.readutf.hermes.packet;

import org.jetbrains.annotations.Nullable;

public class ResponsePacket extends Packet<Void> {

    private final int originalPacketId;
    private final @Nullable Object responseData;

    public ResponsePacket(int originalPacketId, @Nullable Object responseData) {
        super(false);
        this.originalPacketId = originalPacketId;
        this.responseData = responseData;
    }

    public int getOriginalPacketId() {
        return originalPacketId;
    }

    public @Nullable Object getResponseData() {
        return responseData;
    }
}
