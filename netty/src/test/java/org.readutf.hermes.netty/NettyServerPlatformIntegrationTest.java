package org.readutf.hermes.netty;

import com.esotericsoftware.kryo.Kryo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.readutf.hermes.kryo.KryoPacketCodec;
import org.readutf.hermes.packet.Packet;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class NettyServerPlatformIntegrationTest {

    public static class TestPacket implements Packet<Void> {
        public String message;

        public TestPacket(String message) {
            this.message = message;
        }

        @Override
        public boolean expectsResponse() {
            return false;
        }

        @Override
        public int getId() {
            return 0;
        }
    }

    @Test
    void simpleTest() throws Exception {

        Supplier<Kryo> kryoSupplier = () -> {
            Kryo kryo = new Kryo();
            kryo.register(TestPacket.class);
            return kryo;
        };
        NettyServerPlatform serverPlatform = new NettyServerPlatform(new KryoPacketCodec(kryoSupplier));
        serverPlatform.start(new InetSocketAddress(5000));

        NettyClientPlatform clientPlatform = new NettyClientPlatform(new KryoPacketCodec(kryoSupplier));
        clientPlatform.connect(new InetSocketAddress(5000));

        CompletableFuture<Void> future = new CompletableFuture<>();

        serverPlatform.listenIgnore(TestPacket.class, (channel, packet) -> {
            Assertions.assertEquals("Hello World", packet.message);
            future.complete(null);
        });

        clientPlatform.sendPacket(new TestPacket("Hello World"));

        future.get(5, TimeUnit.SECONDS); // Wait for the server to process the packet
    }
}
