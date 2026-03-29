package net.minecraftforge.network;

import java.util.function.Supplier;

public final class PacketDistributor {
    public static final Target<Object> PLAYER = new Target<>();
    public static final Target<Object> TRACKING_CHUNK = new Target<>();
    public static final Target<Object> TRACKING_ENTITY = new Target<>();

    private PacketDistributor() {
    }

    public static final class Target<T> {
        public PacketTarget<T> with(final Supplier<T> supplier) {
            return new PacketTarget<>(supplier.get());
        }
    }

    public static final class PacketTarget<T> {
        private final T value;

        public PacketTarget(final T value) {
            this.value = value;
        }

        public T value() {
            return value;
        }
    }
}
