package net.minecraftforge.network.simple;

import li.cil.oc2.common.network.message.NetworkEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleChannel {
    private final ResourceLocation name;
    private final Supplier<String> protocolVersion;
    private final List<Registration<?>> registrations = new ArrayList<>();
    private final Map<Class<?>, Registration<?>> registrationsByType = new HashMap<>();

    public SimpleChannel(final ResourceLocation name, final Supplier<String> protocolVersion) {
        this.name = name;
        this.protocolVersion = protocolVersion;
    }

    public <T> void sendToServer(final T message) {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(getPayload(message));
    }

    public <T> void send(final Object target, final T message) {
        final MessagePayload<T> payload = getPayload(message);
        if (!(target instanceof final PacketDistributor.PacketTarget<?> packetTarget)) {
            throw new IllegalArgumentException("Unsupported packet target [" + target + "].");
        }

        switch (packetTarget.kind()) {
            case PLAYER -> net.neoforged.neoforge.network.PacketDistributor.sendToPlayer((ServerPlayer) packetTarget.value(), payload);
            case TRACKING_CHUNK -> {
                final LevelChunk chunk = (LevelChunk) packetTarget.value();
                if (chunk.getLevel() instanceof final ServerLevel level) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingChunk(level, chunk.getPos(), payload);
                }
            }
            case TRACKING_ENTITY -> net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity((Entity) packetTarget.value(), payload);
        }
    }

    public <T> void reply(final T message, final NetworkEvent.Context context) {
        context.reply(getPayload(message));
    }

    public <T> MessageBuilder<T> messageBuilder(final Class<T> type, final int id, final NetworkDirection direction) {
        return new MessageBuilder<>(type, id, direction);
    }

    public void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(protocolVersion.get());
        for (final Registration<?> registration : registrations) {
            registration.register(registrar);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> MessagePayload<T> getPayload(final T message) {
        final Registration<T> registration = (Registration<T>) registrationsByType.get(message.getClass());
        if (registration == null) {
            throw new IllegalArgumentException("Unregistered message type [" + message.getClass().getName() + "].");
        }
        return registration.createPayload(message);
    }

    public final class MessageBuilder<T> {
        private final Class<T> type;
        private final int id;
        private final NetworkDirection direction;
        private BiConsumer<T, FriendlyByteBuf> encoder;
        private Function<FriendlyByteBuf, T> decoder;
        private BiFunction<T, Supplier<NetworkEvent.Context>, Boolean> consumer;

        private MessageBuilder(final Class<T> type, final int id, final NetworkDirection direction) {
            this.type = type;
            this.id = id;
            this.direction = direction;
        }

        public MessageBuilder<T> encoder(final BiConsumer<T, FriendlyByteBuf> encoder) {
            this.encoder = encoder;
            return this;
        }

        public MessageBuilder<T> decoder(final Function<FriendlyByteBuf, T> decoder) {
            this.decoder = decoder;
            return this;
        }

        public MessageBuilder<T> consumer(final BiFunction<T, Supplier<NetworkEvent.Context>, Boolean> consumer) {
            this.consumer = consumer;
            return this;
        }

        public void add() {
            final Registration<T> registration = new Registration<>(type, id, direction, encoder, decoder, consumer);
            registrations.add(registration);
            registrationsByType.put(type, registration);
        }
    }

    private final class Registration<T> {
        private final Class<T> messageType;
        private final NetworkDirection direction;
        private final BiConsumer<T, FriendlyByteBuf> encoder;
        private final Function<FriendlyByteBuf, T> decoder;
        private final BiFunction<T, Supplier<NetworkEvent.Context>, Boolean> consumer;
        private final CustomPacketPayload.Type<MessagePayload<T>> payloadType;
        private final StreamCodec<RegistryFriendlyByteBuf, MessagePayload<T>> codec;
        private final IPayloadHandler<MessagePayload<T>> handler;

        private Registration(final Class<T> messageType, final int id, final NetworkDirection direction,
                             final BiConsumer<T, FriendlyByteBuf> encoder, final Function<FriendlyByteBuf, T> decoder,
                             final BiFunction<T, Supplier<NetworkEvent.Context>, Boolean> consumer) {
            this.messageType = messageType;
            this.direction = direction;
            this.encoder = encoder;
            this.decoder = decoder;
            this.consumer = consumer;
            this.payloadType = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(name.getNamespace(), name.getPath() + "/" + id));
            this.codec = StreamCodec.of(this::encode, this::decode);
            this.handler = (payload, context) -> consumer.apply(payload.message(), () -> new NetworkEvent.Context(context));
        }

        private MessagePayload<T> createPayload(final T message) {
            return new MessagePayload<>(payloadType, messageType, message);
        }

        private void register(final PayloadRegistrar registrar) {
            switch (direction) {
                case PLAY_TO_CLIENT -> registrar.playToClient(payloadType, codec, handler);
                case PLAY_TO_SERVER -> registrar.playToServer(payloadType, codec, handler);
            }
        }

        private MessagePayload<T> decode(final RegistryFriendlyByteBuf buffer) {
            return createPayload(decoder.apply(buffer));
        }

        private void encode(final RegistryFriendlyByteBuf buffer, final MessagePayload<T> payload) {
            encoder.accept(payload.message(), buffer);
        }
    }

    private record MessagePayload<T>(CustomPacketPayload.Type<? extends CustomPacketPayload> payloadType, Class<T> messageType, T message) implements CustomPacketPayload {
        @SuppressWarnings("unchecked")
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return payloadType;
        }
    }
}
