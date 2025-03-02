package org.readutf.hermes.listeners

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.hermes.Packet
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.response.ResponseManager
import java.util.concurrent.ExecutorService

internal class ListenerManager(
    private val responseManager: ResponseManager,
    private val executorService: ExecutorService,
) {
    private val logger = KotlinLogging.logger { }

    /**
     * A map of packet types to their corresponding listeners.
     */
    private val registeredListeners: MutableList<Pair<Class<out Packet<*>>, Listener>> = mutableListOf()

    internal fun handlePacket(
        hermesChannel: HermesChannel,
        packet: Packet<*>,
    ) {
        executorService.submit {
            val packetListeners = getListeners(packet)

            for (listener: Listener in packetListeners) {
                logger.debug { "Handling packet with listener ${listener.javaClass.name}" }
                val result: Result<Any?, Throwable> = listener.acceptPacket(hermesChannel, packet)

                if (result.isOk && result.get() == Unit) {
                    logger.debug { "Packet handled successfully and has no response data" }
                    continue
                }
                logger.debug { "Packet handled successfully and has response data" }

                responseManager.sendPacketResponse(packet, result, hermesChannel)

                return@submit
            }
        }
    }

    private fun getListeners(packet: Packet<*>): List<Listener> =
        registeredListeners.filter { (clazz, _) -> clazz.isAssignableFrom(packet.javaClass) }.map { it.second }

    internal fun registerListener(
        clazz: Class<out Packet<*>>,
        vararg listeners: Listener,
    ) {
        for (listener in listeners) {
            registeredListeners.add(clazz to listener)
        }
    }
}
