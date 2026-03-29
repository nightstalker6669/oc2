package net.minecraftforge.network;

import java.util.function.Supplier;

public final class PacketDistributor {
    public static final Target<Object> PLAYER = new Target<>(Kind.PLAYER);
    public static final Target<Object> TRACKING_CHUNK = new Target<>(Kind.TRACKING_CHUNK);
    public static final Target<Object> TRACKING_ENTITY = new Target<>(Kind.TRACKING_ENTITY);

    private PacketDistributor() {
    }

    public enum Kind {
        PLAYER,
        TRACKING_CHUNK,
        TRACKING_ENTITY
    }

    public static final class Target<T> {
        private final Kind kind;

        private Target(final Kind kind) {
            this.kind = kind;
        }

        public PacketTarget<T> with(final Supplier<T> supplier) {
            return new PacketTarget<>(kind, supplier.get());
        }
    }

    public static final class PacketTarget<T> {
        private final Kind kind;
        private final T value;

        public PacketTarget(final Kind kind, final T value) {
            this.kind = kind;
            this.value = value;
        }

        public Kind kind() {
            return kind;
        }

        public T value() {
            return value;
        }
    }
}
