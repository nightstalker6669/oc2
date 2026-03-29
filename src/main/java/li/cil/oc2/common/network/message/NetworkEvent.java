package li.cil.oc2.common.network.message;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;

public final class NetworkEvent {
    private NetworkEvent() {
    }

    public static class Context {
        @Nullable private final IPayloadContext payloadContext;
        @Nullable private final ServerPlayer sender;

        public Context() {
            this(null, null);
        }

        public Context(@Nullable final ServerPlayer sender) {
            this(null, sender);
        }

        public Context(final IPayloadContext payloadContext) {
            this(payloadContext, payloadContext.player() instanceof final ServerPlayer sender ? sender : null);
        }

        private Context(@Nullable final IPayloadContext payloadContext, @Nullable final ServerPlayer sender) {
            this.payloadContext = payloadContext;
            this.sender = sender;
        }

        @Nullable
        public ServerPlayer getSender() {
            return sender;
        }

        public void enqueueWork(final Runnable runnable) {
            if (payloadContext != null) {
                payloadContext.enqueueWork(runnable);
            } else {
                runnable.run();
            }
        }

        public void reply(final CustomPacketPayload payload) {
            if (payloadContext != null) {
                payloadContext.reply(payload);
            }
        }
    }
}
