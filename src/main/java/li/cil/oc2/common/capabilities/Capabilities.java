/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.capabilities;

import li.cil.oc2.api.bus.DeviceBusElement;
import li.cil.oc2.api.bus.device.Device;
import li.cil.oc2.api.capabilities.NetworkInterface;
import li.cil.oc2.api.capabilities.RedstoneEmitter;
import li.cil.oc2.api.capabilities.Robot;
import li.cil.oc2.api.capabilities.TerminalUserProvider;
import li.cil.oc2.common.Config;
import li.cil.oc2.common.blockentity.ModBlockEntity;
import li.cil.oc2.common.energy.EnergyStorageItemStack;
import li.cil.oc2.common.item.RobotItem;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static li.cil.oc2.common.Constants.ENERGY_TAG_NAME;
import static li.cil.oc2.common.Constants.MOD_TAG_NAME;

public final class Capabilities {
    public static Capability<IEnergyStorage> energyStorage() {
        return CapabilityRegistry.ENERGY_STORAGE;
    }

    public static Capability<IFluidHandler> fluidHandler() {
        return CapabilityRegistry.FLUID_HANDLER;
    }

    public static Capability<IItemHandler> itemHandler() {
        return CapabilityRegistry.ITEM_HANDLER;
    }

    public static Capability<DeviceBusElement> deviceBusElement() {
        return CapabilityRegistry.DEVICE_BUS_ELEMENT;
    }

    public static Capability<Device> device() {
        return CapabilityRegistry.DEVICE;
    }

    public static Capability<RedstoneEmitter> redstoneEmitter() {
        return CapabilityRegistry.REDSTONE_EMITTER;
    }

    public static Capability<NetworkInterface> networkInterface() {
        return CapabilityRegistry.NETWORK_INTERFACE;
    }

    public static Capability<TerminalUserProvider> terminalUserProvider() {
        return CapabilityRegistry.TERMINAL_USER_PROVIDER;
    }

    public static Capability<Robot> robot() {
        return CapabilityRegistry.ROBOT;
    }

    public static void registerCapabilities(final Consumer<Class<?>> registry) {
        registry.accept(DeviceBusElement.class);
        registry.accept(Device.class);
        registry.accept(RedstoneEmitter.class);
        registry.accept(NetworkInterface.class);
        registry.accept(TerminalUserProvider.class);
        registry.accept(Robot.class);
    }

    public static <T> LazyOptional<T> getCapability(@Nullable final BlockEntity blockEntity, final Capability<T> capability, @Nullable final Direction side) {
        if (blockEntity instanceof final ModBlockEntity modBlockEntity) {
            return modBlockEntity.getCapability(capability, side);
        }
        if (blockEntity instanceof final ICapabilityProvider provider) {
            return provider.getCapability(capability, side);
        }
        return LazyOptional.empty();
    }

    public static <T> LazyOptional<T> getCapability(@Nullable final Entity entity, final Capability<T> capability, @Nullable final Direction side) {
        if (entity instanceof final li.cil.oc2.common.entity.Robot robotEntity) {
            return robotEntity.getCapability(capability, side);
        }
        if (entity instanceof final ICapabilityProvider provider) {
            return provider.getCapability(capability, side);
        }
        return LazyOptional.empty();
    }

    public static <T> LazyOptional<T> getCapability(final ItemStack stack, final Capability<T> capability) {
        if (capability == energyStorage()) {
            final IEnergyStorage storage = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.ITEM, null);
            if (storage != null) {
                return LazyOptional.of(() -> storage).cast();
            }
            if (stack.getItem() instanceof RobotItem) {
                return LazyOptional.of(() -> new EnergyStorageItemStack(stack, Config.robotEnergyStorage, MOD_TAG_NAME, ENERGY_TAG_NAME)).cast();
            }
        }

        if (capability == itemHandler()) {
            final IItemHandler handler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.ITEM, null);
            if (handler != null) {
                return LazyOptional.of(() -> handler).cast();
            }
        }

        if (capability == fluidHandler()) {
            final IFluidHandler handler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM, null);
            if (handler != null) {
                return LazyOptional.of(() -> handler).cast();
            }
        }

        return LazyOptional.empty();
    }
}
