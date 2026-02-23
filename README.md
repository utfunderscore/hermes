# Hermes

A multi-module Java framework for packet-based communication with support for different transport implementations (Netty) and serialization codecs (Kryo).

## Overview

Hermes provides a clean abstraction for sending and receiving typed packets over network connections. It handles serialization, event dispatching, and request-response patterns out of the box.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Hermes                              │
│            (Abstract base class for platforms)             │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────────┐
│   Packet<R>   │    │ PacketCodec   │    │ PacketEventManager│
│  (Interface)  │    │  (Interface)  │    │                   │
└───────────────┘    └───────────────┘    └───────────────────┘
                              │                     │
                              ▼                     ▼
                    ┌───────────────┐    ┌───────────────────┐
                    │KryoPacketCodec│    │  ResponseManager  │
                    │   (Impl)      │    │                   │
                    └───────────────┘    └───────────────────┘
                                              
                                              
              ┌───────────────────────────────────────────┐
              │              NettyPlatform               │
              │         (Abstract Netty base)           │
              └───────────────────────────────────────────┘
                          │                   │
                          ▼                   ▼
              ┌───────────────────┐  ┌───────────────────┐
              │NettyServerPlatform│  │NettyClientPlatform│
              └───────────────────┘  └───────────────────┘
```

## Modules

| Module | Description |
|--------|-------------|
| `core` | Core framework: Hermes, Packet, Event system, Response handling |
| `kryo` | Kryo-based serialization codec |
| `netty` | Netty server and client implementations |

## Key Components

### Packet<RESPONSE>

All packets implement the `Packet` interface:

```java
public interface Packet<RESPONSE> {
    boolean expectsResponse();
    int getId();
}
```

- `expectsResponse()` - Whether the packet expects a response from the handler
- `getId()` - Unique identifier for the packet type

### PacketCodec

Encodes packets to bytes and decodes bytes back to packets:

```java
public interface PacketCodec {
    Packet<?> decode(byte[] data);
    byte[] encode(Packet<?> packet);
}
```

### Hermes

The main abstract class that handles:
- Sending packets
- Registering listeners
- Handling incoming packets
- Managing responses

## Usage Examples

### 1. Creating a Custom Packet

```java
public class ChatMessagePacket implements Packet<ChatResponse> {
    private final String message;
    private final UUID senderId;

    public ChatMessagePacket(String message, UUID senderId) {
        this.message = message;
        this.senderId = senderId;
    }

    @Override
    public boolean expectsResponse() {
        return true;  // Server should acknowledge
    }

    @Override
    public int getId() {
        return 1;
    }

    public String getMessage() { return message; }
    public UUID getSenderId() { return senderId; }
}

// Response is just a regular class - no need to implement Packet
public class ChatResponse {
    private final boolean success;
    private final String serverMessage;

    public ChatResponse(boolean success, String serverMessage) {
        this.success = success;
        this.serverMessage = serverMessage;
    }

    public boolean isSuccess() { return success; }
    public String getServerMessage() { return serverMessage; }
}
```

### 2. Setting Up a Server

```java
// Create Kryo codec with custom configuration
Supplier<Kryo> kryoSupplier = () -> {
    Kryo kryo = new Kryo();
    kryo.register(ChatMessagePacket.class);
    kryo.register(ChatResponse.class);
    return kryo;
};

PacketCodec codec = new KryoPacketCodec(kryoSupplier);

// Create and start server
NettyServerPlatform server = new NettyServerPlatform(codec);
server.start(new InetSocketAddress(8080));

// Register packet listener
server.listen(ChatMessagePacket.class, (channel, packet) -> {
    System.out.println("Received: " + packet.getMessage());
    return new ChatResponse(true, "Message received!");
});
```

### 3. Connecting as a Client

```java
// Create client with same codec
NettyClientPlatform client = new NettyClientPlatform(codec);

// Connect to server
client.connect(new InetSocketAddress("localhost", 8080));

// Send packet without expecting response
client.sendPacket(new ChatMessagePacket("Hello!", UUID.randomUUID()));

// Send packet with response (async)
CompletableFuture<ChatResponse> response = client.sendResponsePacket(
    new ChatMessagePacket("Hello!", UUID.randomUUID()),
    ChatResponse.class
);

ChatResponse result = response.get(5, TimeUnit.SECONDS);
```

### 4. Listening for Packets (Server-Side)

```java
// Using listenIgnore for packets that don't return responses
server.listenIgnore(TestPacket.class, (channel, packet) -> {
    System.out.println("Received from " + channel.getId() + ": " + packet.message);
});

// Using full listener for packets that expect responses
server.listen(ChatMessagePacket.class, (channel, packet) -> {
    // Process packet and return response
    return new ChatResponse(true, "Processed: " + packet.getMessage());
});
```

### 5. Broadcasting to All Connected Clients

```java
// Access all registered channels from the server
for (Map.Entry<HermesChannel, Channel> entry : server.getHermesToNettyChannel().entrySet()) {
    HermesChannel channel = entry.getKey();
    server.sendPacket(channel, new BroadcastPacket("Hello everyone!"));
}
```

### 6. Handling Client Disconnection

```java
server.listenIgnore(ChannelClosePacket.class, (channel, packet) -> {
    System.out.println("Client disconnected: " + channel.getId());
    // Clean up resources associated with this client
});
```

## Common Patterns

### Request-Response Pattern

```java
// On client side - send request and wait for response
CompletableFuture<ServerResponse> future = client.sendResponsePacket(
    new MyRequestPacket(data),
    ServerResponse.class
);

ServerResponse response = future.get(10, TimeUnit.SECONDS);

// On server side - handle request and return response
hermes.listen(MyRequestPacket.class, (channel, packet) -> {
    // Process request
    return new ServerResponse(result);
});
```

### Event-Driven Architecture

```java
// Register multiple listeners for the same packet type
hermes.listen(PlayerJoinPacket.class, (channel, packet) -> {
    // First listener: notify admins
    notifyAdmins(packet.getPlayerName());
    return null;
});

hermes.listen(PlayerJoinPacket.class, (channel, packet) -> {
    // Second listener: update player list
    updatePlayerList(packet.getPlayerName());
    return null;
});
```

### Custom Codec Implementation

```java
public class JsonPacketCodec implements PacketCodec {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Packet<?> decode(byte[] data) {
        return mapper.readValue(data, Packet.class);
    }

    @Override
    public byte[] encode(Packet<?> packet) {
        return mapper.writeValueAsBytes(packet);
    }
}
```

## Dependencies

### Core Module (Minimal)
- SLF4J API
- JetBrains Annotations

### Kryo Module
- Kryo (serialization)

### Netty Module
- Netty (transport)
- Depends on: core, kryo

## Building

```bash
./gradlew build
```

## Testing

```bash
./gradlew test
```

## Thread Safety

- `PacketEventManager` uses concurrent collections for thread-safe listener registration
- `ResponseManager` uses concurrent maps for thread-safe response handling
- `NettyPlatform` uses concurrent maps for channel management

## Best Practices

1. **Register all packet types** in your Kryo configuration before use
2. **Use meaningful packet IDs** to avoid conflicts
3. **Handle timeouts** when waiting for responses
4. **Clean up resources** by calling `shutdown()` on platforms
5. **Use response packets** for bidirectional communication
