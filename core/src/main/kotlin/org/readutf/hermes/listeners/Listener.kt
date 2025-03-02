package org.readutf.hermes.listeners

import com.github.michaelbull.result.Result
import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel

public interface Listener {
    public fun acceptPacket(
        hermesChannel: HermesChannel,
        packet: Packet<*>,
    ): Result<Any?, Throwable>
}
