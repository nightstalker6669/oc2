/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import li.cil.oc2.api.API;
import li.cil.oc2.common.block.Blocks;
import li.cil.oc2.common.block.ComputerBlock;
import li.cil.oc2.common.util.RegistryUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ItemGroup {
    private static final DeferredRegister<CreativeModeTab> TABS = RegistryUtils.getInitializerFor(Registries.CREATIVE_MODE_TAB);

    public static final RegistryObject<CreativeModeTab> COMMON = TABS.register("common", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + API.MOD_ID + ".common"))
            .icon(() -> new ItemStack(Items.COMPUTER.get()))
            .displayItems((parameters, output) -> {
                add(output, Items.BUS_CABLE.get());
                addFilled(output, (BusInterfaceItem) Items.BUS_INTERFACE.get());
                addFilled(output, (ChargerItem) Items.CHARGER.get());
                addFilled(output, Blocks.COMPUTER.get());
                add(output, Items.CREATIVE_ENERGY.get());
                add(output, Items.DISK_DRIVE.get());
                add(output, Items.KEYBOARD.get());
                add(output, Items.NETWORK_CONNECTOR.get());
                add(output, Items.NETWORK_HUB.get());
                add(output, Items.PROJECTOR.get());
                add(output, Items.REDSTONE_INTERFACE.get());

                add(output, Items.WRENCH.get());
                add(output, Items.MANUAL.get());
                addFilled(output, (RobotItem) Items.ROBOT.get());
                add(output, Items.NETWORK_CABLE.get());

                add(output, Items.MEMORY_SMALL.get());
                add(output, Items.MEMORY_MEDIUM.get());
                add(output, Items.MEMORY_LARGE.get());

                add(output, Items.HARD_DRIVE_SMALL.get());
                add(output, Items.HARD_DRIVE_MEDIUM.get());
                add(output, Items.HARD_DRIVE_LARGE.get());
                addFilled(output, (HardDriveWithExternalDataItem) Items.HARD_DRIVE_CUSTOM.get());

                add(output, Items.FLASH_MEMORY.get());
                add(output, Items.FLASH_MEMORY_CUSTOM.get());
                add(output, Items.FLOPPY.get());

                add(output, Items.REDSTONE_INTERFACE_CARD.get());
                add(output, Items.NETWORK_INTERFACE_CARD.get());
                add(output, Items.NETWORK_TUNNEL_CARD.get());
                add(output, Items.FILE_IMPORT_EXPORT_CARD.get());
                add(output, Items.SOUND_CARD.get());

                add(output, Items.INVENTORY_OPERATIONS_MODULE.get());
                add(output, Items.BLOCK_OPERATIONS_MODULE.get());
                add(output, Items.NETWORK_TUNNEL_MODULE.get());

                add(output, Items.TRANSISTOR.get());
                add(output, Items.CIRCUIT_BOARD.get());
            })
            .build());

    public static void initialize() {
    }

    private static void add(final CreativeModeTab.Output output, final Item item) {
        output.accept(new ItemStack(item));
    }

    private static void addFilled(final CreativeModeTab.Output output, final BusInterfaceItem item) {
        final NonNullList<ItemStack> items = NonNullList.create();
        item.fillItemCategory(COMMON.get(), items);
        items.forEach(output::accept);
    }

    private static void addFilled(final CreativeModeTab.Output output, final ChargerItem item) {
        final NonNullList<ItemStack> items = NonNullList.create();
        item.fillItemCategory(COMMON.get(), items);
        items.forEach(output::accept);
    }

    private static void addFilled(final CreativeModeTab.Output output, final ComputerBlock block) {
        final NonNullList<ItemStack> items = NonNullList.create();
        block.fillItemCategory(COMMON.get(), items);
        items.forEach(output::accept);
    }

    private static void addFilled(final CreativeModeTab.Output output, final RobotItem item) {
        final NonNullList<ItemStack> items = NonNullList.create();
        item.fillItemCategory(COMMON.get(), items);
        items.forEach(output::accept);
    }

    private static void addFilled(final CreativeModeTab.Output output, final HardDriveWithExternalDataItem item) {
        final NonNullList<ItemStack> items = NonNullList.create();
        item.fillItemCategory(COMMON.get(), items);
        items.forEach(output::accept);
    }

    private ItemGroup() {
    }
}
