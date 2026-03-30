/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.bus.device.provider.util;

import li.cil.oc2.api.bus.device.Device;
import li.cil.oc2.api.bus.device.provider.BlockDeviceQuery;
import li.cil.oc2.api.util.Invalidatable;
import li.cil.oc2.common.capabilities.CapabilityRef;
import li.cil.oc2.common.util.LazyValue;
import li.cil.oc2.common.util.LazyValueUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public abstract class AbstractBlockEntityCapabilityDeviceProvider<TCapability, TBlockEntity extends BlockEntity> extends AbstractBlockEntityDeviceProvider<TBlockEntity> {
    private final Supplier<CapabilityRef<TCapability>> capabilitySupplier;

    ///////////////////////////////////////////////////////////////////

    protected AbstractBlockEntityCapabilityDeviceProvider(final BlockEntityType<TBlockEntity> blockEntityType, final Supplier<CapabilityRef<TCapability>> capabilitySupplier) {
        super(blockEntityType);
        this.capabilitySupplier = capabilitySupplier;
    }

    protected AbstractBlockEntityCapabilityDeviceProvider(final Supplier<CapabilityRef<TCapability>> capabilitySupplier) {
        this.capabilitySupplier = capabilitySupplier;
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected final Invalidatable<Device> getBlockDevice(final BlockDeviceQuery query, final BlockEntity blockEntity) {
        final CapabilityRef<TCapability> capability = capabilitySupplier.get();
        if (capability == null) throw new IllegalStateException();
        final LazyValue<TCapability> optional = li.cil.oc2.common.capabilities.Capabilities.getCapability(blockEntity, capability, query.getQuerySide());
        if (!optional.isPresent()) {
            return Invalidatable.empty();
        }

        final TCapability value = optional.orElseThrow(AssertionError::new);
        final Invalidatable<Device> device = getBlockDevice(query, value);

        // When capability gets invalidated, invalidate device. But don't keep device alive via capability.
        LazyValueUtils.addWeakListener(optional, device, (invalidatable, unused) -> invalidatable.invalidate());

        return device;
    }

    protected abstract Invalidatable<Device> getBlockDevice(final BlockDeviceQuery query, final TCapability value);
}
