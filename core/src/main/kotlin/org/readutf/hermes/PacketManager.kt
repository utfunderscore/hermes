package org.readutf.hermes

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.getOrThrow
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.Listener
import org.readutf.hermes.listeners.ListenerManager
import org.readutf.hermes.listeners.TypedListener
import org.readutf.hermes.metrics.HermesMetrics
import org.readutf.hermes.platform.PacketPlatform
import org.readutf.hermes.response.ResponseManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

public class PacketManager<PLATFORM : PacketPlatform<PLATFORM>> private constructor(
    public val packetPlatform: PLATFORM,
    executorService: ExecutorService,
    metricsRegistry: MeterRegistry,
) {
    init {
        HermesMetrics.init(metricsRegistry)
    }

    internal val logger: KLogger = KotlinLogging.logger { }
    public val responseManager: ResponseManager = ResponseManager()
    private val listenerManager: ListenerManager = ListenerManager(responseManager, executorService)

    internal fun init(): Result<PacketManager<PLATFORM>, Throwable> =
        runCatching {
            packetPlatform.setupPacketListener { channel, packet ->
                onPacketReceived(channel, packet)
            }
            packetPlatform.init(this).getOrThrow()
            this
        }

    public fun start(): PacketManager<PLATFORM> {
        packetPlatform.start()
        return this
    }

    /**
     * Send a packet to the other side of the connection.
     */
    public fun sendPacket(packet: Packet<*>): Result<Unit, Throwable> =
        packetPlatform
            .sendPacket(packet)
            .onSuccess {
                HermesMetrics.sentPackets.record(1.0)
            }.onFailure {
                HermesMetrics.failedTransfers.record(1.0)
            }

    public inline fun <reified T> sendPacketFuture(packet: Packet<T>): CompletableFuture<Result<T, Throwable>> {
        packetPlatform.sendPacket(packet).getOrElse { return CompletableFuture.failedFuture(it) }
        return responseManager.getPacketResponseFuture(packet)
    }

    public fun registerListener(
        clazz: Class<out Packet<*>>,
        vararg listener: Listener,
    ): PacketManager<PLATFORM> {
        listenerManager.registerListener(clazz, *listener)
        return this
    }

    public inline fun <reified T : Packet<V>, reified U : HermesChannel, V> registerListener(
        typedListener: TypedListener<T, U, V>,
    ): PacketManager<PLATFORM> {
        registerListener(
            T::class.java,
            object : Listener {
                override fun acceptPacket(
                    hermesChannel: HermesChannel,
                    packet: Packet<*>,
                ) = typedListener.handle(packet as T, hermesChannel as U)
            },
        )
        return this
    }

    public inline fun <reified T : Packet<*>> registerListener(crossinline typedListener: (T) -> Unit): PacketManager<PLATFORM> {
        registerListener(
            T::class.java,
            object : Listener {
                override fun acceptPacket(
                    hermesChannel: HermesChannel,
                    packet: Packet<*>,
                ): Result<Unit, Throwable> =
                    runCatching {
                        typedListener(packet as T)
                    }
            },
        )
        return this
    }

    private fun onPacketReceived(
        hermesChannel: HermesChannel,
        packet: Packet<*>,
    ) {
        logger.debug { "Received packet: $packet" }
        HermesMetrics.receivedPackets.record(1.0)
        listenerManager.handlePacket(hermesChannel, packet)
    }

    public fun stop() {
        packetPlatform.stop()
    }

    public companion object {
        public fun <T : PacketPlatform<T>> create(
            platform: T,
            executorService: ExecutorService,
            meterRegistry: MeterRegistry = SimpleMeterRegistry(),
        ): Result<PacketManager<T>, Throwable> = PacketManager(platform, executorService, meterRegistry).init()
    }
}
