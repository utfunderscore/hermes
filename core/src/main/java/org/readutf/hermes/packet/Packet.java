package org.readutf.hermes.packet;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Packet<RESPONSE> {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    private final int id = ID_GENERATOR.getAndIncrement();
    private final boolean expectsResponse;

    public Packet(boolean expectsResponse) {
        this.expectsResponse = expectsResponse;
    }

    public int getId() {
        return id;
    }

    public boolean expectsResponse() {
        return expectsResponse;
    }
}
