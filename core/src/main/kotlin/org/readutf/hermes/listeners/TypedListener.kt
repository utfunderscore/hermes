package org.readutf.hermes.listeners

import com.github.michaelbull.result.Result
import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel

interface TypedListener<T : Packet<V>, U : HermesChannel, V> {
    fun handle(
        packet: T,
        channel: U,
    ): Result<V, Throwable>
}
