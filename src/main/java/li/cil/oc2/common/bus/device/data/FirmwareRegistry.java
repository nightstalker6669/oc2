/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.bus.device.data;

import li.cil.oc2.api.bus.device.data.Firmware;
import li.cil.oc2.api.util.Registries;
import li.cil.oc2.common.registry.RegistryView;
import li.cil.oc2.common.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class FirmwareRegistry {
    private static final DeferredRegister<Firmware> INITIALIZER = RegistryUtils.getInitializerFor(Registries.FIRMWARE);

    ///////////////////////////////////////////////////////////////////

    private static final Supplier<RegistryView<Firmware>> REGISTRY = RegistryUtils.makeRegistryView(INITIALIZER);

    ///////////////////////////////////////////////////////////////////

    public static final DeferredHolder<Firmware, Firmware> BUILDROOT = INITIALIZER.register("buildroot", BuildrootFirmware::new);

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
    }

    @Nullable
    public static ResourceLocation getKey(final Firmware firmware) {
        return REGISTRY.get().getKey(firmware);
    }

    @Nullable
    public static Firmware getValue(final ResourceLocation location) {
        return REGISTRY.get().getValue(location);
    }

    public static Stream<Firmware> values() {
        return REGISTRY.get().getValues().stream();
    }
}
