package org.readutf.hermes.packet;

/**
 * Represents a generic packet that can be transmitted over a communication channel.
 * Each packet has an associated unique identifier and may optionally expect a response.
 *
 * @param <RESPONSE> the type of response expected by this packet, if any
 */
public interface Packet<RESPONSE> {

    boolean expectsResponse();

    int getId();
}
