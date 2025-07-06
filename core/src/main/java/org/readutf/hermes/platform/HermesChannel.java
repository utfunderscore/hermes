package org.readutf.hermes.platform;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HermesChannel {

    private final @NotNull UUID channelId;

    public HermesChannel() {
        this.channelId = UUID.randomUUID();
    }

    public @NotNull UUID getId() {
        return channelId;
    }
}
