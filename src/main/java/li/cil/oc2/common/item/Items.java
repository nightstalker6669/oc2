/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import li.cil.oc2.common.Constants;
import li.cil.oc2.common.block.Blocks;
import li.cil.oc2.common.bus.device.data.BlockDeviceDataRegistry;
import li.cil.oc2.common.bus.device.data.FirmwareRegistry;
import li.cil.oc2.common.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public final class Items {
    private static final DeferredRegister<Item> ITEMS = RegistryUtils.getInitializerFor(Registries.ITEM);

    ///////////////////////////////////////////////////////////////////

    public static final DeferredHolder<Item, Item> BUS_CABLE = register(Blocks.BUS_CABLE, BusCableItem::new);
    public static final DeferredHolder<Item, BusInterfaceItem> BUS_INTERFACE = register("bus_interface", BusInterfaceItem::new);
    public static final DeferredHolder<Item, Item> CHARGER = register(Blocks.CHARGER, ChargerItem::new);
    public static final DeferredHolder<Item, Item> COMPUTER = register(Blocks.COMPUTER);
    public static final DeferredHolder<Item, Item> CREATIVE_ENERGY = register(Blocks.CREATIVE_ENERGY);
    public static final DeferredHolder<Item, Item> DISK_DRIVE = register(Blocks.DISK_DRIVE);
    public static final DeferredHolder<Item, Item> KEYBOARD = register(Blocks.KEYBOARD);
    public static final DeferredHolder<Item, Item> NETWORK_CONNECTOR = register(Blocks.NETWORK_CONNECTOR);
    public static final DeferredHolder<Item, Item> NETWORK_HUB = register(Blocks.NETWORK_HUB);
    public static final DeferredHolder<Item, Item> PROJECTOR = register(Blocks.PROJECTOR);
    public static final DeferredHolder<Item, Item> REDSTONE_INTERFACE = register(Blocks.REDSTONE_INTERFACE);

    ///////////////////////////////////////////////////////////////////

    public static final DeferredHolder<Item, Item> WRENCH = register("wrench", WrenchItem::new);
    public static final DeferredHolder<Item, Item> MANUAL = register("manual", Items::createManualItem);

    public static final DeferredHolder<Item, Item> ROBOT = register("robot", RobotItem::new);
    public static final DeferredHolder<Item, NetworkCableItem> NETWORK_CABLE = register("network_cable", NetworkCableItem::new);

    public static final DeferredHolder<Item, MemoryItem> MEMORY_SMALL = register("memory_small", () ->
        new MemoryItem(2 * Constants.MEGABYTE));
    public static final DeferredHolder<Item, MemoryItem> MEMORY_MEDIUM = register("memory_medium", () ->
        new MemoryItem(4 * Constants.MEGABYTE));
    public static final DeferredHolder<Item, MemoryItem> MEMORY_LARGE = register("memory_large", () ->
        new MemoryItem(8 * Constants.MEGABYTE));

    public static final DeferredHolder<Item, HardDriveItem> HARD_DRIVE_SMALL = register("hard_drive_small", () ->
        new HardDriveItem(2 * Constants.MEGABYTE, DyeColor.LIGHT_GRAY));
    public static final DeferredHolder<Item, HardDriveItem> HARD_DRIVE_MEDIUM = register("hard_drive_medium", () ->
        new HardDriveItem(4 * Constants.MEGABYTE, DyeColor.GREEN));
    public static final DeferredHolder<Item, HardDriveItem> HARD_DRIVE_LARGE = register("hard_drive_large", () ->
        new HardDriveItem(8 * Constants.MEGABYTE, DyeColor.CYAN));
    public static final DeferredHolder<Item, HardDriveWithExternalDataItem> HARD_DRIVE_CUSTOM = register("hard_drive_custom", () ->
        new HardDriveWithExternalDataItem(BlockDeviceDataRegistry.BUILDROOT.getId(), DyeColor.BROWN));

    public static final DeferredHolder<Item, FlashMemoryItem> FLASH_MEMORY = register("flash_memory", () ->
        new FlashMemoryItem(4 * Constants.KILOBYTE));
    public static final DeferredHolder<Item, FlashMemoryWithExternalDataItem> FLASH_MEMORY_CUSTOM = register("flash_memory_custom", () ->
        new FlashMemoryWithExternalDataItem(FirmwareRegistry.BUILDROOT.getId()));

    public static final DeferredHolder<Item, FloppyItem> FLOPPY = register("floppy", () ->
        new FloppyItem(512 * Constants.KILOBYTE));

    public static final DeferredHolder<Item, Item> REDSTONE_INTERFACE_CARD = register("redstone_interface_card");
    public static final DeferredHolder<Item, Item> NETWORK_INTERFACE_CARD = register("network_interface_card", NetworkInterfaceCardItem::new);
    public static final DeferredHolder<Item, Item> NETWORK_TUNNEL_CARD = register("network_tunnel_card", NetworkTunnelItem::new);
    public static final DeferredHolder<Item, Item> FILE_IMPORT_EXPORT_CARD = register("file_import_export_card");
    public static final DeferredHolder<Item, Item> SOUND_CARD = register("sound_card");

    public static final DeferredHolder<Item, Item> INVENTORY_OPERATIONS_MODULE = register("inventory_operations_module");
    public static final DeferredHolder<Item, Item> BLOCK_OPERATIONS_MODULE = register("block_operations_module", BlockOperationsModule::new);
    public static final DeferredHolder<Item, Item> NETWORK_TUNNEL_MODULE = register("network_tunnel_module", NetworkTunnelItem::new);

    public static final DeferredHolder<Item, Item> TRANSISTOR = register("transistor", ModItem::new);
    public static final DeferredHolder<Item, Item> CIRCUIT_BOARD = register("circuit_board", ModItem::new);

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
        ItemRenameHandler.registerAliases(ITEMS);
    }

    ///////////////////////////////////////////////////////////////////

    private static DeferredHolder<Item, Item> register(final String name) {
        return register(name, ModItem::new);
    }

    private static <T extends Item> DeferredHolder<Item, T> register(final String name, final Supplier<T> factory) {
        return ITEMS.register(name, factory);
    }

    private static Item createManualItem() {
        if (ModList.get().isLoaded("markdown_manual")) {
            try {
                final Class<?> manualItemClass = Class.forName("li.cil.oc2.common.item.ManualItem");
                return (Item) manualItemClass.getConstructor().newInstance();
            } catch (final ReflectiveOperationException | LinkageError ignored) {
            }
        }

        return new ModItem();
    }

    private static <T extends Block> DeferredHolder<Item, Item> register(final DeferredHolder<Block, T> block) {
        return register(block, ModBlockItem::new);
    }

    private static <TBlock extends Block, TItem extends Item> DeferredHolder<Item, TItem> register(final DeferredHolder<Block, TBlock> block, final Function<TBlock, TItem> factory) {
        return register(block.getId().getPath(), () -> factory.apply(block.get()));
    }
}
