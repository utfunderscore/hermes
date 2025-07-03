package org.readutf.hermes.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import org.jetbrains.annotations.NotNull;
import org.readutf.hermes.packet.Packet;
import org.readutf.hermes.codec.PacketCodec;

import java.io.ByteArrayOutputStream;
import java.util.function.Supplier;

public class KryoPacketCodec implements PacketCodec {

    private final @NotNull Pool<Kryo> kryoSupplier;

    public KryoPacketCodec(@NotNull Supplier<Kryo> kryoSupplier) {
        this.kryoSupplier = getKryoSupplier(kryoSupplier);
    }

    public Pool<Kryo> getKryoSupplier(@NotNull Supplier<Kryo> kryoSupplier) {
        return new Pool<>(true, true) {
            @Override
            protected Kryo create() {
                Kryo kryo = kryoSupplier.get();

                // Register classes here if needed
                return kryo;
            }
        };
    }

    @Override
    public Packet<?> decode(byte[] data) {
        Kryo kryo = kryoSupplier.obtain();
        Packet<?> packet = null;
        try (Input input = new Input(data)) {
            packet = (Packet<?>) kryo.readClassAndObject(input);
        } finally {
            kryoSupplier.free(kryo);
        }

        return packet;
    }

    @Override
    public byte[] encode(Packet<?> packet) {

        Kryo kryo = kryoSupplier.obtain();
        byte[] data;
        try (Output output = new Output(new ByteArrayOutputStream())) {
            kryo.writeClassAndObject(output, packet);
            data = output.toBytes();
        } finally {
            kryoSupplier.free(kryo);
        }

        return data;

    }
}
