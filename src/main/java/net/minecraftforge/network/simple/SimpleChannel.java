package net.minecraftforge.network.simple;

import li.cil.oc2.common.network.message.NetworkEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleChannel {
    private final ResourceLocation name;
    private final Supplier<String> protocolVersion;

    public SimpleChannel(final ResourceLocation name, final Supplier<String> protocolVersion) {
        this.name = name;
        this.protocolVersion = protocolVersion;
    }

    public <T> void sendToServer(final T message) {
    }

    public <T> void send(final Object target, final T message) {
    }

    public <T> void reply(final T message, final NetworkEvent.Context context) {
    }

    public <T> MessageBuilder<T> messageBuilder(final Class<T> type, final int id, final NetworkDirection direction) {
        return new MessageBuilder<>();
    }

    public static final class MessageBuilder<T> {
        public MessageBuilder<T> encoder(final BiConsumer<T, FriendlyByteBuf> encoder) {
            return this;
        }

        public MessageBuilder<T> decoder(final Function<FriendlyByteBuf, T> decoder) {
            return this;
        }

        public MessageBuilder<T> consumer(final BiFunction<T, Supplier<NetworkEvent.Context>, Boolean> consumer) {
            return this;
        }

        public void add() {
        }
    }
}
