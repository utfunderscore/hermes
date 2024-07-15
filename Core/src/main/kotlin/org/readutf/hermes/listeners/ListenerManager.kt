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

    fun registerAll(scannedObject: Any) {
        val kClass: KClass<*> = scannedObject::class
        kClass.memberFunctions.forEach { function ->
            val packetHandler = function.findAnnotation<PacketHandler>() ?: return@forEach

            val parameters = function.valueParameters

            if (parameters.size == 1) {
                if (!parameters[0]
                        .type.jvmErasure.java
                        .isAssignableFrom(Packet::class.java)
                ) {
                    logger.error { "Parameter is not a packet in listener ${function.name}" }
                    return
                }

                val packetClass: Class<out Packet> =
                    parameters[0]
                        .type.jvmErasure.java
                        .asSubclass(Packet::class.java)

                val listener =
                    object : Listener {
                        override fun acceptPacket(
                            hermesChannel: HermesChannel,
                            packet: Packet,
                        ) {
                            function.call(scannedObject, packet)
                        }
                    }

                registerListener(packetClass, listener)
            } else if (parameters.size == 2) {
                val channelIndex =
                    parameters.indexOfFirst { kParameter ->
                        kParameter.type.jvmErasure.java
                            .isAssignableFrom(HermesChannel::class.java)
                    }
                val packetIndex =
                    parameters.indexOfFirst { kParameter ->
                        kParameter.type.jvmErasure.java
                            .isAssignableFrom(Packet::class.java)
                    }

                if (packetIndex == -1 || channelIndex == -1) {
                    logger.error { "Listener ${function.name} does not have a channel or packet parameter" }
                    return
                }

                val packetType =
                    parameters[packetIndex]
                        .type.jvmErasure.java
                        .asSubclass(Packet::class.java)

                val listener =
                    object : Listener {
                        override fun acceptPacket(
                            hermesChannel: HermesChannel,
                            packet: Packet,
                        ) {
                            val args = arrayOfNulls<Any>(3)
                            args[0] = scannedObject
                            args[channelIndex] = hermesChannel
                            args[packetIndex] = packet
                        }
                    }

                registerListener(packetType, listener)
            }
        }
    }
}
