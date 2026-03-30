/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.bus.device.provider;

import li.cil.oc2.api.bus.device.provider.BlockDeviceProvider;
import li.cil.oc2.api.bus.device.provider.ItemDeviceProvider;
import li.cil.oc2.api.util.Registries;
import li.cil.oc2.common.registry.RegistryView;
import li.cil.oc2.common.util.RegistryUtils;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ProviderRegistry {
    private static final DeferredRegister<BlockDeviceProvider> BLOCK_DEVICE_PROVIDERS = RegistryUtils.getInitializerFor(Registries.BLOCK_DEVICE_PROVIDER);
    public static final Supplier<RegistryView<BlockDeviceProvider>> BLOCK_DEVICE_PROVIDER_REGISTRY = RegistryUtils.makeRegistryView(BLOCK_DEVICE_PROVIDERS);

    ///////////////////////////////////////////////////////////////////

    private static final DeferredRegister<ItemDeviceProvider> ITEM_DEVICE_PROVIDERS = RegistryUtils.getInitializerFor(Registries.ITEM_DEVICE_PROVIDER);
    public static final Supplier<RegistryView<ItemDeviceProvider>> ITEM_DEVICE_PROVIDER_REGISTRY = RegistryUtils.makeRegistryView(ITEM_DEVICE_PROVIDERS);

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
        Providers.registerBlockDeviceProviders(BLOCK_DEVICE_PROVIDERS::register);
        Providers.registerItemDeviceProviders(ITEM_DEVICE_PROVIDERS::register);
    }
}
