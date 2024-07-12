package org.readutf.hermes.listeners

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.listeners.annotation.PacketHandler
import org.readutf.hermes.Packet
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

class ListenerManager {
    private val logger = KotlinLogging.logger { }
    private val listeners = mutableMapOf<Class<out Packet>, MutableList<Listener>>()

    fun invokeListeners(packet: Packet) {
        listeners.entries.forEach { (clazz, packetListeners) ->
            if (clazz.isAssignableFrom(packet.javaClass)) {
                packetListeners.forEach { listener: Listener -> listener.acceptPacket(packet) }
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
                override fun acceptPacket(packet: Packet) {
                    typedListener(packet as T)
                }
            },
        )
    }

    fun registerAll(any: Any) {
        val kClass: KClass<*> = any::class
        kClass.memberFunctions.forEach { function ->
            val packetHandler = function.findAnnotation<PacketHandler>() ?: return@forEach

            if (function.valueParameters.size != 1) {
                logger.error { "Function ${function.name} must only have a packet as a parameter." }
                return@forEach
            }
            val parameter = function.valueParameters[0]
            val parameterType = parameter.type.jvmErasure.java
            if (!Packet::class.java.isAssignableFrom(parameterType)) {
                logger.error { "Function ${function.name} must only have a packet as a parameter." }
                return@forEach
            }

            val listener =
                object : Listener {
                    override fun acceptPacket(packet: Packet) {
                        function.call(any, packet)
                    }
                }

            logger.info { "Registering listener for packet type: ${parameterType.simpleName}" }
            registerListener(parameterType.asSubclass(Packet::class.java), listener)
        }
    }
}
