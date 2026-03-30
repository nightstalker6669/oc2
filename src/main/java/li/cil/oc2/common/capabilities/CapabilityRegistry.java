/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.capabilities;

import li.cil.oc2.api.bus.DeviceBusElement;
import li.cil.oc2.api.bus.device.Device;
import li.cil.oc2.api.capabilities.NetworkInterface;
import li.cil.oc2.api.capabilities.RedstoneEmitter;
import li.cil.oc2.api.capabilities.Robot;
import li.cil.oc2.api.capabilities.TerminalUserProvider;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public final class CapabilityRegistry {
    static final CapabilityRef<IEnergyStorage> ENERGY_STORAGE = new CapabilityRef<>();
    static final CapabilityRef<IFluidHandler> FLUID_HANDLER = new CapabilityRef<>();
    static final CapabilityRef<IItemHandler> ITEM_HANDLER = new CapabilityRef<>();

    static final CapabilityRef<DeviceBusElement> DEVICE_BUS_ELEMENT = new CapabilityRef<>();
    static final CapabilityRef<Device> DEVICE = new CapabilityRef<>();
    static final CapabilityRef<RedstoneEmitter> REDSTONE_EMITTER = new CapabilityRef<>();
    static final CapabilityRef<NetworkInterface> NETWORK_INTERFACE = new CapabilityRef<>();
    static final CapabilityRef<TerminalUserProvider> TERMINAL_USER_PROVIDER = new CapabilityRef<>();
    static final CapabilityRef<Robot> ROBOT = new CapabilityRef<>();
}
