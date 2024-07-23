package org.readutf.hermes

import java.util.concurrent.atomic.AtomicInteger

abstract class Packet(
    val packetId: Int = currentId.getAndIncrement(),
) {
    companion object {
        val currentId = AtomicInteger(0)
    }
}
