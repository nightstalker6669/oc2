/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.block;

import li.cil.oc2.common.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Blocks {
    private static final DeferredRegister<Block> BLOCKS = RegistryUtils.getInitializerFor(Registries.BLOCK);

    ///////////////////////////////////////////////////////////////////

    public static final DeferredHolder<Block, BusCableBlock> BUS_CABLE = BLOCKS.register("bus_cable", () -> new BusCableBlock());
    public static final DeferredHolder<Block, ChargerBlock> CHARGER = BLOCKS.register("charger", ChargerBlock::new);
    public static final DeferredHolder<Block, ComputerBlock> COMPUTER = BLOCKS.register("computer", () -> new ComputerBlock());
    public static final DeferredHolder<Block, CreativeEnergyBlock> CREATIVE_ENERGY = BLOCKS.register("creative_energy", CreativeEnergyBlock::new);
    public static final DeferredHolder<Block, DiskDriveBlock> DISK_DRIVE = BLOCKS.register("disk_drive", () -> new DiskDriveBlock());
    public static final DeferredHolder<Block, KeyboardBlock> KEYBOARD = BLOCKS.register("keyboard", () -> new KeyboardBlock());
    public static final DeferredHolder<Block, NetworkConnectorBlock> NETWORK_CONNECTOR = BLOCKS.register("network_connector", () -> new NetworkConnectorBlock());
    public static final DeferredHolder<Block, NetworkHubBlock> NETWORK_HUB = BLOCKS.register("network_hub", () -> new NetworkHubBlock());
    public static final DeferredHolder<Block, ProjectorBlock> PROJECTOR = BLOCKS.register("projector", () -> new ProjectorBlock());
    public static final DeferredHolder<Block, RedstoneInterfaceBlock> REDSTONE_INTERFACE = BLOCKS.register("redstone_interface", () -> new RedstoneInterfaceBlock());

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
    }
}
