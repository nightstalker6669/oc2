/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.integration.jade;

import li.cil.oc2.api.API;
import li.cil.oc2.common.Constants;
import li.cil.oc2.common.capabilities.Capabilities;
import li.cil.oc2.common.entity.Robot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import javax.annotation.Nullable;
import java.util.Objects;

import static li.cil.oc2.common.util.TextFormatUtils.withFormat;

public final class MachineEntityComponentProvider implements IEntityComponentProvider {
    public static final MachineEntityComponentProvider INSTANCE = new MachineEntityComponentProvider();

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "robot");
    private static final ResourceLocation STATUS_LINE = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "robot_status");
    private static final ResourceLocation ENERGY_LINE = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "robot_energy");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(@Nullable final ITooltip tooltip, @Nullable final EntityAccessor accessor, @Nullable final IPluginConfig config) {
        final ITooltip actualTooltip = Objects.requireNonNull(tooltip);
        if (!(Objects.requireNonNull(accessor).getEntity() instanceof final Robot robot)) {
            return;
        }

        final Component error = robot.getVirtualMachine().getError();
        if (error != null) {
            actualTooltip.add(withFormat(error.copy(), ChatFormatting.RED), STATUS_LINE);
        } else if (robot.getVirtualMachine().isRunning()) {
            actualTooltip.add(withFormat(Component.translatable("tooltip.oc2.status.running"), ChatFormatting.GREEN), STATUS_LINE);
        }

        Capabilities.getCapability(robot, Capabilities.energyStorage(), null).ifPresent(energy -> {
            if (energy.getMaxEnergyStored() <= 0) {
                return;
            }

            final MutableComponent value = withFormat(energy.getEnergyStored() + "/" + energy.getMaxEnergyStored(), ChatFormatting.GREEN);
            actualTooltip.add(withFormat(Component.translatable(Constants.TOOLTIP_ENERGY, value), ChatFormatting.GRAY), ENERGY_LINE);
        });
    }

    private MachineEntityComponentProvider() {
    }
}
