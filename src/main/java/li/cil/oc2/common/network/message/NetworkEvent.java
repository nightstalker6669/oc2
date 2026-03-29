package li.cil.oc2.common.network.message;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public final class NetworkEvent {
    private NetworkEvent() {
    }

    public static class Context {
        @Nullable private final ServerPlayer sender;

        public Context() {
            this(null);
        }

        public Context(@Nullable final ServerPlayer sender) {
            this.sender = sender;
        }

        @Nullable
        public ServerPlayer getSender() {
            return sender;
        }

        public void enqueueWork(final Runnable runnable) {
            runnable.run();
        }
    }
}
