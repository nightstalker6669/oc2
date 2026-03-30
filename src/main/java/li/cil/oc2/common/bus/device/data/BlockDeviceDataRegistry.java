/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.bus.device.data;

import li.cil.oc2.api.bus.device.data.BlockDeviceData;
import li.cil.oc2.api.util.Registries;
import li.cil.oc2.common.registry.RegistryView;
import li.cil.oc2.common.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class BlockDeviceDataRegistry {
    private static final DeferredRegister<BlockDeviceData> INITIALIZER = RegistryUtils.getInitializerFor(Registries.BLOCK_DEVICE_DATA);

    ///////////////////////////////////////////////////////////////////

    private static final Supplier<RegistryView<BlockDeviceData>> REGISTRY = RegistryUtils.makeRegistryView(INITIALIZER);

    ///////////////////////////////////////////////////////////////////

    public static final DeferredHolder<BlockDeviceData, BlockDeviceData> BUILDROOT = INITIALIZER.register("buildroot", BuildrootBlockDeviceData::new);

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
    }

    @Nullable
    public static ResourceLocation getKey(final BlockDeviceData data) {
        final ResourceLocation value = REGISTRY.get().getKey(data);
        if (value != null) {
            return value;
        }
        if (data instanceof final ResourceBlockDeviceData resourceData) {
            return resourceData.getLocation();
        }
        return FileSystems.getBlockData().entrySet().stream()
            .filter(entry -> entry.getValue() == data)
            .map(java.util.Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    @Nullable
    public static BlockDeviceData getValue(final ResourceLocation location) {
        final BlockDeviceData value = REGISTRY.get().getValue(location);
        if (value != null) {
            return value;
        }
        return FileSystems.getBlockData().get(location);
    }

    public static Stream<BlockDeviceData> values() {
        return Stream.concat(
            REGISTRY.get().getValues().stream(),
            FileSystems.getBlockData().values().stream());
    }
}
