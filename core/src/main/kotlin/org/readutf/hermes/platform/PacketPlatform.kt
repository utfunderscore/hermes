package org.readutf.hermes.platform

import com.github.michaelbull.result.Result
import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.serializer.PacketSerializer
import java.util.function.BiConsumer

public interface PacketPlatform<PLATFORM : PacketPlatform<PLATFORM>> {
    public fun init(packetManager: PacketManager<PLATFORM>): Result<Unit, Throwable>

    public fun setupPacketListener(packetConsumer: BiConsumer<HermesChannel, Packet<*>>)

    public fun sendPacket(packet: Packet<*>): Result<Unit, Throwable>

    public fun getChannel(channelId: String): HermesChannel?

    public fun setSerializer(serializer: PacketSerializer)

    public fun start()

    public fun stop()
}
