package org.readutf.hermes.platform

import org.readutf.hermes.Packet
import org.readutf.hermes.serializer.PacketSerializer
import java.util.function.Consumer

interface PacketPlatform {
    fun setupPacketListener(packetConsumer: Consumer<Packet>)

    fun sendPacket(packet: Packet)

    fun setSerializer(serializer: PacketSerializer)

    fun start()

    fun stop()
}
