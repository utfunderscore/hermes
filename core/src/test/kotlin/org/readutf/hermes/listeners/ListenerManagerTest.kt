package org.readutf.hermes.listeners

import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.readutf.hermes.Packet
import org.readutf.hermes.listeners.annotation.PacketHandler
import java.util.concurrent.Executors

class ListenerManagerTest {
    private var listenerManager: ListenerManager = ListenerManager(Executors.newSingleThreadScheduledExecutor())
    private var logger = KotlinLogging.logger { }

    @BeforeEach
    fun beforeEach() {
        listenerManager = ListenerManager(Executors.newSingleThreadScheduledExecutor())
    }

    @Test
    fun testAnnotationPacketListener() {
        listenerManager.registerAll(TestListener())

//        listenerManager.handlePacket(
//            object : HermesChannel(UUID.randomUUID().toString(), ) {
//                override fun sendPacket(packet: Packet) {
//                }
//            },
//            TestPacket(),
//        )
    }

    @Test
    fun registerAll() {
    }

    class TestPacket : Packet()

    class TestPacket2 : Packet()

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
