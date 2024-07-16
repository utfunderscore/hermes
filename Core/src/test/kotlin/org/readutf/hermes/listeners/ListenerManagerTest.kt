package org.readutf.hermes.listeners

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.annotation.PacketHandler
import java.util.UUID

class ListenerManagerTest {
    private var listenerManager: ListenerManager = ListenerManager()

    @BeforeEach
    fun beforeEach() {
        listenerManager = ListenerManager()
    }

    @Test
    fun testAnnotationPacketListener() {
        listenerManager.registerAll(TestListener())

        listenerManager.handlePacket(
            object : HermesChannel(UUID.randomUUID().toString()) {
                override fun sendPacket(packet: Packet) {
                }
            },
            TestPacket(),
        )
    }

    @Test
    fun registerAll() {
    }

    class TestPacket : Packet

    class TestPacket2 : Packet

    class TestListener {
        @PacketHandler
        fun onTestPacket(packet: TestPacket) {
            println("Packet received")
        }

        @PacketHandler
        fun onTestPacket2(packet: TestPacket2) {
            println("2 received")
        }
    }
}
