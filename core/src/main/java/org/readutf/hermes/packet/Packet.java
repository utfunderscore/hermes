package org.readutf.hermes.packet;

import java.util.concurrent.atomic.AtomicInteger;

public interface Packet<RESPONSE> {

    AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    boolean expectsResponse();

    int getId();
}
