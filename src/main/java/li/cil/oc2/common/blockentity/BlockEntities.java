/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.blockentity;

import li.cil.oc2.common.block.Blocks;
import li.cil.oc2.common.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = RegistryUtils.getInitializerFor(Registries.BLOCK_ENTITY_TYPE);

    ///////////////////////////////////////////////////////////////////

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BusCableBlockEntity>> BUS_CABLE = register(Blocks.BUS_CABLE, BusCableBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChargerBlockEntity>> CHARGER = register(Blocks.CHARGER, ChargerBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ComputerBlockEntity>> COMPUTER = register(Blocks.COMPUTER, ComputerBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeEnergyBlockEntity>> CREATIVE_ENERGY = register(Blocks.CREATIVE_ENERGY, CreativeEnergyBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DiskDriveBlockEntity>> DISK_DRIVE = register(Blocks.DISK_DRIVE, DiskDriveBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KeyboardBlockEntity>> KEYBOARD = register(Blocks.KEYBOARD, KeyboardBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NetworkConnectorBlockEntity>> NETWORK_CONNECTOR = register(Blocks.NETWORK_CONNECTOR, NetworkConnectorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NetworkHubBlockEntity>> NETWORK_HUB = register(Blocks.NETWORK_HUB, NetworkHubBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProjectorBlockEntity>> PROJECTOR = register(Blocks.PROJECTOR, ProjectorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneInterfaceBlockEntity>> REDSTONE_INTERFACE = register(Blocks.REDSTONE_INTERFACE, RedstoneInterfaceBlockEntity::new);

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
    }

    ///////////////////////////////////////////////////////////////////

    private static <B extends Block, T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(final DeferredHolder<Block, B> block, final BlockEntityType.BlockEntitySupplier<T> factory) {
        return BLOCK_ENTITIES.register(block.getId().getPath(), () -> buildType(factory, block.get()));
    }

    @SuppressWarnings("null")
    private static <T extends BlockEntity> BlockEntityType<T> buildType(final BlockEntityType.BlockEntitySupplier<T> factory, final Block block) {
        return BlockEntityType.Builder.of(factory, block).build(null);
    }
}
