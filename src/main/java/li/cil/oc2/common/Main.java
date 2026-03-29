/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common;

import li.cil.ceres.Ceres;
import li.cil.oc2.api.API;
import li.cil.oc2.client.ClientSetup;
import li.cil.oc2.common.block.Blocks;
import li.cil.oc2.common.blockentity.BlockEntities;
import li.cil.oc2.common.bus.device.DeviceTypes;
import li.cil.oc2.common.bus.device.data.BlockDeviceDataRegistry;
import li.cil.oc2.common.bus.device.data.FirmwareRegistry;
import li.cil.oc2.common.bus.device.provider.ProviderRegistry;
import li.cil.oc2.common.container.Containers;
import li.cil.oc2.common.entity.Entities;
import li.cil.oc2.common.integration.IMC;
import li.cil.oc2.common.item.ItemRenameHandler;
import li.cil.oc2.common.item.ItemGroup;
import li.cil.oc2.common.item.Items;
import li.cil.oc2.common.item.crafting.RecipeSerializers;
import li.cil.oc2.common.network.Network;
import li.cil.oc2.common.serialization.ceres.Serializers;
import li.cil.oc2.common.tags.BlockTags;
import li.cil.oc2.common.tags.ItemTags;
import li.cil.oc2.common.util.RegistryUtils;
import li.cil.oc2.common.util.SoundEvents;
import li.cil.oc2.common.vm.provider.DeviceTreeProviders;
import li.cil.sedna.Sedna;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(API.MOD_ID)
public final class Main {
    public Main(final IEventBus modEventBus, final ModContainer modContainer) {
        Ceres.initialize();
        Sedna.initialize();
        DeviceTreeProviders.initialize();
        Serializers.initialize();
        Network.initialize();

        ConfigManager.add(Config::new);
        ConfigManager.initialize(modContainer);

        RegistryUtils.begin(modEventBus);

        ItemTags.initialize();
        BlockTags.initialize();
        Blocks.initialize();
        Items.initialize();
        ItemGroup.initialize();
        BlockEntities.initialize();
        Entities.initialize();
        Containers.initialize();
        RecipeSerializers.initialize();
        SoundEvents.initialize();

        ProviderRegistry.initialize();
        DeviceTypes.initialize();
        BlockDeviceDataRegistry.initialize();
        FirmwareRegistry.initialize();

        if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("markdown_manual")) {
            initializeManuals();
        }

        RegistryUtils.finish();

        ItemRenameHandler.initialize();

        modEventBus.register(CommonSetup.class);
        modEventBus.register(Network.class);
        IMC.initialize(modEventBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.register(ClientSetup.class);
        }
    }

    private static void initializeManuals() {
        try {
            final Class<?> manuals = Class.forName("li.cil.oc2.client.manual.Manuals");
            manuals.getMethod("initialize").invoke(null);
        } catch (final ReflectiveOperationException ignored) {
        }
    }
}
