package org.readutf.hermes

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.ListenerManager
import org.readutf.hermes.platform.PacketPlatform
import java.util.function.Consumer

class PacketManager<T : PacketPlatform>(
    private val packetPlatform: T,
) {
    private val logger = KotlinLogging.logger { }
    private val listenerManager = ListenerManager()

    init {
        packetPlatform.setupPacketListener { channel, packet ->
            onPacketReceived(channel, packet)
        }
    }

    fun sendPacket(packet: Packet) {
        packetPlatform.sendPacket(packet)
    }

    fun start(): PacketManager<T> {
        packetPlatform.start()
        return this
    }

    fun editListeners(consumer: Consumer<ListenerManager>): PacketManager<T> {
        consumer.accept(listenerManager)
        return this
    }

    private fun onPacketReceived(
        hermesChannel: HermesChannel,
        packet: Packet,
    ) {
        logger.info { "Received packet: $packet" }
        listenerManager.invokeListeners(hermesChannel, packet)
    }

    fun stop() {
        packetPlatform.stop()
    }

    companion object {
        fun <T : PacketPlatform> create(platform: T): PacketManager<T> = PacketManager(platform)
    }
}
