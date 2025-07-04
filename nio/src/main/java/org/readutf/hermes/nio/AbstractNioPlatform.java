package org.readutf.hermes.nio;

import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Nullable;
import org.readutf.hermes.packet.ChannelClosePacket;
import org.readutf.hermes.platform.Channel;
import org.readutf.hermes.Hermes;
import org.readutf.hermes.codec.PacketCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractNioPlatform extends Hermes {

    private static final Logger log = LoggerFactory.getLogger(AbstractNioPlatform.class);
    protected final Selector selector;
    protected final AtomicBoolean running;
    protected final Map<Channel, SocketChannel> channelToNio;
    protected final Map<SocketChannel, Channel> nioToChannel;
    protected final Thread shutdownHook = new Thread(() -> {
        try {
            log.info("Shutting down Packet platform...");
            disconnect();
        } catch (IOException e) {
            log.error("Error during shutdown", e);
        }
    });

    private @Nullable Thread thread;

    // Buffer for partial reads per channel
    private final Map<SocketChannel, ByteBuffer> readBuffers = new ConcurrentHashMap<>();

    public AbstractNioPlatform(PacketCodec codec) throws IOException {
        super(codec);
        this.selector = Selector.open();
        this.channelToNio = new ConcurrentHashMap<>();
        this.nioToChannel = new ConcurrentHashMap<>();
        this.running = new AtomicBoolean(false);
    }

    @Override
    @Blocking
    protected void start(InetSocketAddress address) throws Exception {
        initialize(address);
        running.set(true);

        this.thread = new Thread(() -> {
            while (running.get()) {
                try {
                    selector.select();
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
                        handleSelectionKey(key);
                    }
                } catch (RuntimeException | IOException e) {
                    log.error("Error in NIO selector loop", e);
                }
            }
        });

        thread.start();

        Runtime.getRuntime().addShutdownHook(shutdownHook);

        log.debug("NIO platform connected to {}", address);
    }

    /**
     * Subclass must setup channels and register initial interest ops.
     */
    protected abstract void initialize(InetSocketAddress address) throws IOException;

    /**
     * Subclass must handle specific selection events.
     */
    protected abstract void handleSelectionKey(SelectionKey key) throws IOException;


    @Override
    protected void writeData(Channel channel, byte[] packetData) {

        SocketChannel nioChannel = channelToNio.get(channel);
        if (nioChannel == null) {
            log.warn("No NIO channel found for {}", channel);
            return;
        }
        log.debug("Writing to channel {}", channel);
        ByteBuffer buffer = ByteBuffer.allocate(4 + packetData.length);
        buffer.putInt(packetData.length);
        buffer.put(packetData);
        buffer.flip();
        try {
            while (buffer.hasRemaining()) {
                nioChannel.write(buffer);
            }
        } catch (IOException e) {
            handleDisconnect(nioChannel);
            log.error("Error writing to channel {}", channel, e);
            try {
                nioChannel.close();
            } catch (IOException e1) {
                log.error("Error closing channel after write failure", e1);
            }
        }
    }

    protected void readFromChannel(SocketChannel channel) throws IOException {
        ByteBuffer readBuffer = readBuffers.computeIfAbsent(channel, c -> ByteBuffer.allocate(4096));
        ByteBuffer tempBuffer = ByteBuffer.allocate(1024);
        int read;
        log.debug("Reading from channel {}", channel);
        try {
            read = channel.read(tempBuffer);
        } catch (IOException e) {
            log.error("Error reading from channel {}", channel, e);
            handleDisconnect(channel);
            channel.close();
            readBuffers.remove(channel);
            return;
        }
        if (read == -1) {
            handleDisconnect(channel);
            channel.close();
            readBuffers.remove(channel);
            log.info("Channel {} closed by remote host", channel);
            return;
        }
        tempBuffer.flip();
        readBuffer.put(tempBuffer);
        readBuffer.flip();
        while (readBuffer.remaining() >= 4) {
            readBuffer.mark();
            int length = readBuffer.getInt();
            if (readBuffer.remaining() < length) {
                readBuffer.reset();
                break;
            }
            byte[] data = new byte[length];
            readBuffer.get(data);

            @Nullable Channel hermesChannel = nioToChannel.get(channel);
            if (hermesChannel != null) {
                readData(hermesChannel, data);
            } else {
                log.info("Received packet for unknown channel: {}", channel);
            }
        }
        readBuffer.compact();
    }

    protected void handleConnection(SocketChannel channel) {
        log.info("New connection established: {}", channel);

        Channel hermesChannel = new Channel(UUID.randomUUID().toString());
        channelToNio.put(hermesChannel, channel);
        nioToChannel.put(channel, hermesChannel);
    }

    public void handleDisconnect(SocketChannel channel) {
        Channel hermesChannel = nioToChannel.remove(channel);
        if (hermesChannel != null) {
            handlePacket(hermesChannel, new ChannelClosePacket(hermesChannel));
            channelToNio.remove(hermesChannel);
        }
    }

    public void disconnect() throws IOException {
        running.set(false);
        selector.close();
    }
}