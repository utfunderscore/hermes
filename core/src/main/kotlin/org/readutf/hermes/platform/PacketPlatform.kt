package org.readutf.hermes.platform

import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.serializer.PacketSerializer
import java.util.function.BiConsumer

interface PacketPlatform {
    fun init(packetManager: PacketManager<*>)

    fun setupPacketListener(packetConsumer: BiConsumer<HermesChannel, Packet>)

    fun sendPacket(packet: Packet)

    fun getChannel(channelId: String): HermesChannel?

    fun setSerializer(serializer: PacketSerializer)

    fun handleException(throwable: Throwable) {
    }

    fun start()

    fun stop()
}
