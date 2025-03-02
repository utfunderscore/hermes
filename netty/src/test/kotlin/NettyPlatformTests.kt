import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getOrThrow
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test
import org.readutf.hermes.Packet
import org.readutf.hermes.PacketManager
import org.readutf.hermes.channel.HermesChannel
import org.readutf.hermes.listeners.TypedListener
import org.readutf.hermes.platform.netty.nettyClient
import org.readutf.hermes.platform.netty.nettyServer
import org.readutf.hermes.serializer.JacksonPacketSerializer
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class NettyPlatformTests {
    private val logger = KotlinLogging.logger { }

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `test netty start server`() {
        logger.info { "Starting netty server" }
        val server =
            PacketManager
                .nettyServer(
                    hostName = "0.0.0.0",
                    port = 1543,
                    serializer = JacksonPacketSerializer(objectMapper),
                ).getOrThrow()

        server.start()
    }

    @Test
    fun `test netty start client`() {
        logger.info { "Starting netty server" }
        val server =
            PacketManager
                .nettyServer(
                    hostName = "0.0.0.0",
                    port = 1234,
                    serializer = JacksonPacketSerializer(objectMapper),
                ).getOrThrow()
        server.start()

        logger.info { "Starting netty client" }
        val client =
            PacketManager
                .nettyClient(
                    hostName = "0.0.0.0",
                    port = 1234,
                    serializer = JacksonPacketSerializer(objectMapper),
                ).getOrThrow()

        client.start()
    }

    @Test
    fun `test netty packet response`() {
        logger.info { "Starting netty server" }
        val server =
            PacketManager
                .nettyServer(
                    hostName = "0.0.0.0",
                    port = 1234,
                    serializer = JacksonPacketSerializer(objectMapper),
                ).getOrThrow()
                .registerListener(
                    object : TypedListener<DemoPacketWithResponse, HermesChannel, String> {
                        override fun handle(
                            packet: DemoPacketWithResponse,
                            channel: HermesChannel,
                        ): Result<String, Throwable> = Ok(packet.data)
                    },
                )

        server.start()

        logger.info { "Starting netty client" }
        val client =
            PacketManager
                .nettyClient(
                    hostName = "0.0.0.0",
                    port = 1234,
                    serializer = JacksonPacketSerializer(objectMapper),
                ).getOrThrow()

        client.start()

        val packetResponse =
            client
                .sendPacketFuture<String>(DemoPacketWithResponse("Hello, world!"))
                .orTimeout(3000, TimeUnit.MILLISECONDS)
                .join()

        println("response: $packetResponse")

        assertEquals("Hello, world!", packetResponse.get())
    }

    data class DemoPacketWithResponse(
        val data: String,
    ) : Packet<String>()
}
