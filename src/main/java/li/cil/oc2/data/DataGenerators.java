/* SPDX-License-Identifier: MIT */

package li.cil.oc2.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public final class DataGenerators {
    @SubscribeEvent
    public static void gatherData(final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
        final PackOutput packOutput = generator.getPackOutput();
        final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        final ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        if (event.includeServer()) {
            generator.addProvider(true, new ModLootTableProvider(packOutput, lookupProvider));
            final BlockTagsProvider blockTagProvider = new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
            generator.addProvider(true, blockTagProvider);
            generator.addProvider(true, new ModItemTagsProvider(packOutput, lookupProvider, blockTagProvider, existingFileHelper));
            generator.addProvider(true, new ModRecipesProvider(packOutput, lookupProvider));
        }
        if (event.includeClient()) {
            generator.addProvider(true, new ModBlockStateProvider(packOutput, existingFileHelper));
            generator.addProvider(true, new ModItemModelProvider(packOutput, existingFileHelper));
        }
    }
}
