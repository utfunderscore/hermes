package org.readutf.hermes.listeners

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.annotation.PacketHandler
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

class ListenerManager {
    private val logger = KotlinLogging.logger { }
    private val listeners = mutableMapOf<Class<out Packet>, MutableList<Listener>>()

    fun invokeListeners(
        hermesChannel: HermesChannel,
        packet: Packet,
    ) {
        listeners.entries.forEach { (clazz, packetListeners) ->
            if (clazz.isAssignableFrom(packet.javaClass)) {
                packetListeners.forEach { listener: Listener -> listener.acceptPacket(hermesChannel, packet) }
            }
        }
    }

    fun registerListener(
        clazz: Class<out Packet>,
        vararg listener: Listener,
    ) {
        val currentListeners = listeners.getOrDefault(clazz, mutableListOf())
        currentListeners.addAll(listener)
        listeners[clazz] = currentListeners
    }

    inline fun <reified T : Packet> registerListener(crossinline typedListener: (T) -> Unit) {
        registerListener(
            T::class.java,
            object : Listener {
                override fun acceptPacket(
                    hermesChannel: HermesChannel,
                    packet: Packet,
                ) {
                    typedListener(packet as T)
                }
            },
        )
    }

    fun registerAll(any: Any) {
        val kClass: KClass<*> = any::class
        kClass.memberFunctions.forEach { function ->
            val packetHandler = function.findAnnotation<PacketHandler>() ?: return@forEach

            val parameters = function.valueParameters

            var packetClass: Class<out Packet> = Packet::class.java
            var channelIndex = 0
            var packetIndex = 1

            parameters.forEachIndexed { index, kParameter ->
                val javaClass = kParameter.type.jvmErasure.java
                if (javaClass.isAssignableFrom(HermesChannel::class.java)) {
                    channelIndex = index
                    packetClass = javaClass.asSubclass(Packet::class.java)
                } else if (javaClass.isAssignableFrom(Packet::class.java)) {
                    packetIndex = index
                } else {
                    logger.error { "Invalid parameter type ${kParameter.type.jvmErasure}" }
                    return@forEach
                }
            }

            val listener =
                object : Listener {
                    override fun acceptPacket(
                        hermesChannel: HermesChannel,
                        packet: Packet,
                    ) {
                        val args =
                            arrayOf(
                                any,
                                if (channelIndex == 0) packet else hermesChannel,
                                if (packetIndex == 1) hermesChannel else packet,
                            )

                        function.call(*args)
                    }
                }

            logger.info { "Registering packet listener '${function.name}'" }
            registerListener(packetClass, listener)
        }
    }
}
