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

        public TestPacket() {
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

    public static class ResponsePacket implements Packet<String> {
        private final String response;

        public ResponsePacket(String response) {
            this.response = response;
        }

        public ResponsePacket() {
            this.response = null;
        }

        @Override
        public boolean expectsResponse() {
            return true;
        }

        @Override
        public int getId() {
            return 1;
        }

        public String getResponse() {
            return response;
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

        future.get(5, TimeUnit.SECONDS);
    }

    @Test
    void responseTest() throws Exception {

        Supplier<Kryo> kryoSupplier = () -> {
            Kryo kryo = new Kryo();
            kryo.register(TestPacket.class);
            kryo.register(ResponsePacket.class);
            return kryo;
        };
        NettyServerPlatform serverPlatform = new NettyServerPlatform(new KryoPacketCodec(kryoSupplier));
        serverPlatform.start(new InetSocketAddress(5001));

        NettyClientPlatform clientPlatform = new NettyClientPlatform(new KryoPacketCodec(kryoSupplier));
        clientPlatform.connect(new InetSocketAddress(5001));

        serverPlatform.listen(ResponsePacket.class, (channel, packet) -> {
            return "Server received: " + packet.getResponse();
        });

        CompletableFuture<String> responseFuture = clientPlatform.sendResponsePacket(
            new ResponsePacket("Hello from client"),
            String.class
        );

        String response = responseFuture.get(5, TimeUnit.SECONDS);
        Assertions.assertEquals("Server received: Hello from client", response);
    }
}
