/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.integration.jade;

import li.cil.oc2.api.API;
import li.cil.oc2.common.Config;
import li.cil.oc2.common.Constants;
import li.cil.oc2.common.blockentity.ChargerBlockEntity;
import li.cil.oc2.common.blockentity.ComputerBlockEntity;
import li.cil.oc2.common.blockentity.ProjectorBlockEntity;
import li.cil.oc2.common.capabilities.Capabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static li.cil.oc2.common.util.TextFormatUtils.withFormat;

public final class MachineBlockComponentProvider implements IBlockComponentProvider {
    public static final MachineBlockComponentProvider INSTANCE = new MachineBlockComponentProvider();

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "machine");
    private static final ResourceLocation STATUS_LINE = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "machine_status");
    private static final ResourceLocation ENERGY_LINE = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "machine_energy");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(final ITooltip tooltip, final BlockAccessor accessor, final IPluginConfig config) {
        final BlockEntity blockEntity = accessor.getBlockEntity();
        if (blockEntity == null) {
            return;
        }

        addStatus(tooltip, blockEntity);
        addEnergy(tooltip, blockEntity);
    }

    private static void addStatus(final ITooltip tooltip, final BlockEntity blockEntity) {
        if (blockEntity instanceof final ComputerBlockEntity computer) {
            final Component error = computer.getVirtualMachine().getError();
            if (error != null) {
                tooltip.add(withFormat(error.copy(), ChatFormatting.RED), STATUS_LINE);
            } else if (computer.getVirtualMachine().isRunning()) {
                tooltip.add(withFormat(Component.translatable("tooltip.oc2.status.running"), ChatFormatting.GREEN), STATUS_LINE);
            }
            return;
        }

        if (blockEntity instanceof final ChargerBlockEntity charger) {
            if (charger.isCharging()) {
                tooltip.add(withFormat(Component.translatable("tooltip.oc2.status.charging"), ChatFormatting.GREEN), STATUS_LINE);
            }
            return;
        }

        if (blockEntity instanceof final ProjectorBlockEntity projector) {
            if (projector.isProjecting()) {
                tooltip.add(withFormat(Component.translatable("tooltip.oc2.status.projecting"), ChatFormatting.GREEN), STATUS_LINE);
            } else if (Config.projectorsUseEnergy() && !projector.hasEnergy()) {
                tooltip.add(withFormat(Component.translatable(Constants.COMPUTER_ERROR_NOT_ENOUGH_ENERGY), ChatFormatting.RED), STATUS_LINE);
            }
        }
    }

    private static void addEnergy(final ITooltip tooltip, final BlockEntity blockEntity) {
        Capabilities.getCapability(blockEntity, Capabilities.energyStorage(), null).ifPresent(energy -> {
            if (energy.getMaxEnergyStored() <= 0) {
                return;
            }

            final MutableComponent value = withFormat(energy.getEnergyStored() + "/" + energy.getMaxEnergyStored(), ChatFormatting.GREEN);
            tooltip.add(withFormat(Component.translatable(Constants.TOOLTIP_ENERGY, value), ChatFormatting.GRAY), ENERGY_LINE);
        });
    }

    private MachineBlockComponentProvider() {
    }
}
