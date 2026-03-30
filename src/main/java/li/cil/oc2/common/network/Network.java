/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.network;

import li.cil.oc2.api.API;
import li.cil.oc2.common.network.message.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Network {
    private static final String PROTOCOL_VERSION = "1";
    private static final ResourceLocation CHANNEL_NAME = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "main");

    private static int nextPacketId = 1;
    private static boolean initialized;
    private static final List<Registration<?>> REGISTRATIONS = new ArrayList<>();
    private static final Map<Class<?>, Registration<?>> REGISTRATIONS_BY_TYPE = new HashMap<>();

    private Network() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        registerMessage(ComputerTerminalOutputMessage.class, ComputerTerminalOutputMessage::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(ComputerTerminalInputMessage.class, ComputerTerminalInputMessage::new, PayloadDirection.PLAY_TO_SERVER);
        registerMessage(ComputerRunStateMessage.class, ComputerRunStateMessage::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(ComputerBusStateMessage.class, ComputerBusStateMessage::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(ComputerBootErrorMessage.class, ComputerBootErrorMessage::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(ComputerPowerMessage.class, ComputerPowerMessage::new, PayloadDirection.PLAY_TO_SERVER);
        registerMessage(OpenComputerInventoryMessage.class, OpenComputerInventoryMessage::new, PayloadDirection.PLAY_TO_SERVER);
        registerMessage(OpenComputerTerminalMessage.class, OpenComputerTerminalMessage::new, PayloadDirection.PLAY_TO_SERVER);

        registerMessage(NetworkConnectorConnectionsMessage.class, NetworkConnectorConnectionsMessage::new, PayloadDirection.PLAY_TO_CLIENT);

        registerMessage(RobotTerminalOutputMessage.class, RobotTerminalOutputMessage::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(RobotTerminalInputMessage.class, RobotTerminalInputMessage::new, PayloadDirection.PLAY_TO_SERVER);
        registerMessage(RobotRunStateMessage.class, RobotRunStateMessage::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(RobotBusStateMessage.class, RobotBusStateMessage::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(RobotBootErrorMessage.class, RobotBootErrorMessage::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(RobotPowerMessage.class, RobotPowerMessage::new, PayloadDirection.PLAY_TO_SERVER);
        registerMessage(RobotInitializationRequestMessage.class, RobotInitializationRequestMessage::new, PayloadDirection.PLAY_TO_SERVER);
        registerMessage(RobotInitializationMessage.class, RobotInitializationMessage::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(OpenRobotInventoryMessage.class, OpenRobotInventoryMessage::new, PayloadDirection.PLAY_TO_SERVER);
        registerMessage(OpenRobotTerminalMessage.class, OpenRobotTerminalMessage::new, PayloadDirection.PLAY_TO_SERVER);

        registerMessage(DiskDriveFloppyMessage.class, DiskDriveFloppyMessage::new, PayloadDirection.PLAY_TO_CLIENT);

        registerMessage(BusInterfaceNameMessage.ToClient.class, BusInterfaceNameMessage.ToClient::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(BusInterfaceNameMessage.ToServer.class, BusInterfaceNameMessage.ToServer::new, PayloadDirection.PLAY_TO_SERVER);

        registerMessage(ExportedFileMessage.class, ExportedFileMessage::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(RequestImportedFileMessage.class, RequestImportedFileMessage::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(ImportedFileMessage.class, ImportedFileMessage::new, PayloadDirection.PLAY_TO_SERVER);
        registerMessage(ServerCanceledImportFileMessage.class, ServerCanceledImportFileMessage::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(ClientCanceledImportFileMessage.class, ClientCanceledImportFileMessage::new, PayloadDirection.PLAY_TO_SERVER);

        registerMessage(BusCableFacadeMessage.class, BusCableFacadeMessage::new, PayloadDirection.PLAY_TO_CLIENT);

        registerMessage(NetworkInterfaceCardConfigurationMessage.class, NetworkInterfaceCardConfigurationMessage::new, PayloadDirection.PLAY_TO_SERVER);
        registerMessage(NetworkTunnelLinkMessage.class, NetworkTunnelLinkMessage::new, PayloadDirection.PLAY_TO_SERVER);

        registerMessage(ProjectorRequestFramebufferMessage.class, ProjectorRequestFramebufferMessage::new, PayloadDirection.PLAY_TO_SERVER);
        registerMessage(ProjectorFramebufferMessage.class, ProjectorFramebufferMessage::new, PayloadDirection.PLAY_TO_CLIENT);
        registerMessage(ProjectorStateMessage.class, ProjectorStateMessage::new, PayloadDirection.PLAY_TO_CLIENT);

        registerMessage(KeyboardInputMessage.class, KeyboardInputMessage::new, PayloadDirection.PLAY_TO_SERVER);

        registerMessage(MultipartMessage.class, MultipartMessage::new, PayloadDirection.PLAY_TO_SERVER);

        MultipartMessage.registerMessage(ImportedFileMessage.class, ImportedFileMessage::new);
    }

    public static <T extends AbstractMessage> void sendToServer(final T message) {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(getPayload(message));
    }

    public static <T extends AbstractMessage> void sendToClient(final T message, final ServerPlayer player) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, getPayload(message));
    }

    public static <T extends AbstractMessage> void sendToClientsTrackingChunk(final T message, final LevelChunk chunk) {
        if (chunk.getLevel() instanceof final ServerLevel level) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingChunk(level, chunk.getPos(), getPayload(message));
        }
    }

    public static <T extends AbstractMessage> void sendToClientsTrackingBlockEntity(final T message, final BlockEntity blockEntity) {
        final Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        final MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }

        if (!server.isSameThread()) {
            throw new IllegalStateException(
                "Attempting to send network message to BlockEntity from non-server " +
                    "thread [" + Thread.currentThread() + "]. This is not supported, " +
                    "because looking up the chunk from the level is required. " +
                    "Consider caching the containing chunk and using " +
                    "sendToClientsTrackingChunk() directly, instead.");
        }

        final BlockPos blockPos = blockEntity.getBlockPos();
        final int chunkX = SectionPos.blockToSectionCoord(blockPos.getX());
        final int chunkZ = SectionPos.blockToSectionCoord(blockPos.getZ());
        if (level.hasChunk(chunkX, chunkZ)) {
            sendToClientsTrackingChunk(message, level.getChunk(chunkX, chunkZ));
        }
    }

    public static <T extends AbstractMessage> void sendToClientsTrackingEntity(final T message, final Entity entity) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(entity, getPayload(message));
    }

    public static <T extends AbstractMessage> void reply(final T message, final NetworkEvent.Context context) {
        context.reply(getPayload(message));
    }

    private static <T extends AbstractMessage> void registerMessage(final Class<T> type, final Function<FriendlyByteBuf, T> decoder, final PayloadDirection direction) {
        final Registration<T> registration = new Registration<>(type, getNextPacketId(), direction, AbstractMessage::handleMessage, decoder);
        REGISTRATIONS.add(registration);
        REGISTRATIONS_BY_TYPE.put(type, registration);
    }

    private static int getNextPacketId() {
        return nextPacketId++;
    }

    @SubscribeEvent
    public static void handleRegisterPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        for (final Registration<?> registration : REGISTRATIONS) {
            registration.register(registrar);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends AbstractMessage> MessagePayload<T> getPayload(final T message) {
        final Registration<T> registration = (Registration<T>) REGISTRATIONS_BY_TYPE.get(message.getClass());
        if (registration == null) {
            throw new IllegalArgumentException("Unregistered message type [" + message.getClass().getName() + "].");
        }

        return registration.createPayload(message);
    }

    private enum PayloadDirection {
        PLAY_TO_CLIENT,
        PLAY_TO_SERVER
    }

    private record MessagePayload<T>(CustomPacketPayload.Type<? extends CustomPacketPayload> payloadType, T message) implements CustomPacketPayload {
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return payloadType;
        }
    }

    private static final class Registration<T extends AbstractMessage> {
        private final CustomPacketPayload.Type<MessagePayload<T>> payloadType;
        private final PayloadDirection direction;
        private final StreamCodec<RegistryFriendlyByteBuf, MessagePayload<T>> codec;
        private final IPayloadHandler<MessagePayload<T>> handler;

        private Registration(final Class<T> messageType, final int id, final PayloadDirection direction,
                             final BiFunction<T, Supplier<NetworkEvent.Context>, Boolean> consumer,
                             final Function<FriendlyByteBuf, T> decoder) {
            this.payloadType = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CHANNEL_NAME.getNamespace(), CHANNEL_NAME.getPath() + "/" + id));
            this.direction = direction;
            this.codec = StreamCodec.of(
                (buffer, payload) -> payload.message().toBytes(buffer),
                buffer -> new MessagePayload<>(payloadType, decoder.apply(buffer))
            );
            this.handler = (payload, context) -> consumer.apply(payload.message(), () -> new NetworkEvent.Context(context));
        }

        private MessagePayload<T> createPayload(final T message) {
            return new MessagePayload<>(payloadType, message);
        }

        private void register(final PayloadRegistrar registrar) {
            switch (direction) {
                case PLAY_TO_CLIENT -> registrar.playToClient(payloadType, codec, handler);
                case PLAY_TO_SERVER -> registrar.playToServer(payloadType, codec, handler);
            }
        }
    }
}
