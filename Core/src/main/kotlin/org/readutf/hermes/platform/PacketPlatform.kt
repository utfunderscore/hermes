package org.readutf.hermes.platform

import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.serializer.PacketSerializer
import java.util.function.BiConsumer

interface PacketPlatform {
    fun setupPacketListener(packetConsumer: BiConsumer<HermesChannel, Packet>)

    fun sendPacket(packet: Packet)

    fun setSerializer(serializer: PacketSerializer)

    fun start()

    fun stop()
}
