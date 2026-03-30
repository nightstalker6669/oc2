/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.container;

import li.cil.oc2.common.blockentity.ComputerBlockEntity;
import li.cil.oc2.common.bus.CommonDeviceBusController;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkHooks;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class ComputerTerminalContainer extends AbstractComputerContainer {
    public static void createServer(final ComputerBlockEntity computer, final IEnergyStorage energy, final CommonDeviceBusController busController, final ServerPlayer player) {
        NetworkHooks.openGui(player, new SimpleMenuProvider(
            (id, inventory, menuPlayer) -> new ComputerTerminalContainer(id, menuPlayer, computer, createEnergyInfo(energy, busController)),
            Component.translatable(computer.getBlockState().getBlock().getDescriptionId())
        ), computer.getBlockPos());
    }

    public static ComputerTerminalContainer createClient(final int id, final Inventory inventory, final FriendlyByteBuf data) {
        final BlockPos pos = data.readBlockPos();
        final BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof final ComputerBlockEntity computer) {
            return new ComputerTerminalContainer(id, inventory.player, computer, createClientEnergyInfo());
        }

        throw new IllegalArgumentException();
    }

    ///////////////////////////////////////////////////////////////////

    private ComputerTerminalContainer(final int id, final Player player, final ComputerBlockEntity computer, final IntPrecisionContainerData energyInfo) {
        super(Containers.COMPUTER_TERMINAL.get(), id, player, computer, energyInfo);
    }
}
