package org.readutf.hermes.nio;

import org.readutf.hermes.codec.PacketCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;

public class NioServerPlatform extends AbstractNioPlatform {


    private static final Logger log = LoggerFactory.getLogger(NioServerPlatform.class);

    public NioServerPlatform(PacketCodec codec) throws IOException {
        super(codec);
    }

    public void start(InetSocketAddress address) throws Exception {
        super.start(address);
    }

    @Override
    protected void initialize(InetSocketAddress address) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(address);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    protected void handleSelectionKey(SelectionKey key) throws IOException {

        if (!key.isValid()) {
            log.debug("Invalid selection key");
            return;
        }
        if (key.isAcceptable()) {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = ssc.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            handleConnection(clientChannel);
        }
        if (key.isReadable()) {
            readFromChannel((SocketChannel) key.channel());
        }
    }
}