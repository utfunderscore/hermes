package org.readutf.hermes.nio;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.readutf.hermes.packet.Packet;
import org.readutf.hermes.kryo.KryoPacketCodec;
import org.readutf.hermes.packet.ResponsePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class NioIntegrationTests {

    private static final Logger log = LoggerFactory.getLogger(NioIntegrationTests.class);

    public static class TestPacket extends Packet<Void> {
        private String message;

        public TestPacket(String message) {
            super(false);
            this.message = message;
        }

        @Override
        public String toString() {
            return "TestPacket{" + "message='" + message + '\'' +
                    '}';
        }
    }

    public static class TestResponsePacket extends Packet<String> {
        private String responseMessage;

        public TestResponsePacket(String responseMessage) {
            super(true);
            this.responseMessage = responseMessage;
        }

        public String getResponseMessage() {
            return responseMessage;
        }

        @Override
        public String toString() {
            return "TestResponsePacket{" + "responseMessage='" + responseMessage + '\'' +
                    '}';
        }
    }

    @Test
    public void test() throws Exception {

        KryoPacketCodec codec = new KryoPacketCodec(() -> {
            Kryo kryo = new Kryo();
            kryo.register(TestPacket.class);
            kryo.register(TestResponsePacket.class);
            return kryo;
        });

        NioServerPlatform nioServerPlatform = new NioServerPlatform(codec);

        nioServerPlatform.listenIgnore(TestPacket.class, (channel, packet) -> {
            log.info("Received packet event: {}", packet);
        });

        nioServerPlatform.listen(TestResponsePacket.class, (channel, packet) -> {
            log.info("Received response packet event: {}", packet);
            return "hello"; // No response needed
        });

        nioServerPlatform.start(new InetSocketAddress(25555));

        NioClientPlatform nioClientPlatform = new NioClientPlatform(codec);
        nioClientPlatform.connect(new InetSocketAddress(25555));
        nioClientPlatform.sendPacket(new TestPacket("Hello from client!"));

        String response = nioClientPlatform.sendResponsePacket(new TestResponsePacket("Hello from client!"), String.class).join();

        Assertions.assertEquals("hello", response);
    }

}
