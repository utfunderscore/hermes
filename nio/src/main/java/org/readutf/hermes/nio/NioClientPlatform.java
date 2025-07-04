package org.readutf.hermes.nio;

import org.jetbrains.annotations.Nullable;
import org.readutf.hermes.packet.ChannelClosePacket;
import org.readutf.hermes.platform.Channel;
import org.readutf.hermes.codec.PacketCodec;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.CompletableFuture;

public class NioClientPlatform extends AbstractNioPlatform {

    private @Nullable SocketChannel socketChannel;

    public NioClientPlatform(PacketCodec codec) throws IOException {
        super(codec);
    }

    public void connect(InetSocketAddress address) throws Exception {
        super.start(address);
    }

    public void connectBlocking(InetSocketAddress address) throws Exception {
        connect(address);

        CompletableFuture<Void> future = new CompletableFuture<>();
        listenIgnore(ChannelClosePacket.class, (channel, event) -> future.complete(null));
        future.join();
    }

    @Override
    protected void initialize(InetSocketAddress address) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(address);
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }

    @Override
    protected void handleConnection(SocketChannel channel) {
        super.handleConnection(channel);
        this.socketChannel = channel;
    }

    @Override
    protected void handleSelectionKey(SelectionKey key) throws IOException {

        if (!key.isValid()) return;
        if (key.isConnectable()) {
            SocketChannel sc = (SocketChannel) key.channel();
            if (sc.finishConnect()) {
                sc.register(selector, SelectionKey.OP_READ);
                handleConnection(sc);
            }
        }
        if (key.isReadable()) {
            readFromChannel((SocketChannel) key.channel());
        }
    }

    @Override
    protected void writeData(byte[] packetData) throws Exception {
        if (socketChannel != null) {
            Channel channel = nioToChannel.get(socketChannel);
            if (channel == null) {
                throw new IllegalStateException("Channel not found for the socket channel.");
            }
            writeData(channel, packetData);
        } else {
            throw new IllegalStateException("Socket channel is not connected.");
        }
    }
}