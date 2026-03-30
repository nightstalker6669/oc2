/* SPDX-License-Identifier: MIT */

package li.cil.oc2.data;

import li.cil.oc2.api.API;
import li.cil.oc2.common.block.Blocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyCustomDataFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static li.cil.oc2.common.Constants.BLOCK_ENTITY_TAG_NAME_IN_ITEM;
import static li.cil.oc2.common.Constants.ENERGY_TAG_NAME;
import static li.cil.oc2.common.Constants.ITEMS_TAG_NAME;

public final class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Set.of(), List.of(new SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)), lookupProvider);
    }

    public static final class ModBlockLootTables extends BlockLootSubProvider {
        public ModBlockLootTables(final HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            dropSelf(Blocks.CHARGER.get());
            add(Blocks.COMPUTER.get(), this::droppingWithInventory);
            dropSelf(Blocks.CREATIVE_ENERGY.get());
            dropSelf(Blocks.DISK_DRIVE.get());
            dropSelf(Blocks.KEYBOARD.get());
            dropSelf(Blocks.NETWORK_CONNECTOR.get());
            dropSelf(Blocks.NETWORK_HUB.get());
            dropSelf(Blocks.PROJECTOR.get());
            dropSelf(Blocks.REDSTONE_INTERFACE.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return BuiltInRegistries.BLOCK.stream()
                .filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(API.MOD_ID))
                .filter(block -> block != Blocks.BUS_CABLE.get())
                .collect(Collectors.toList());
        }

        @SuppressWarnings("deprecation")
        private LootTable.Builder droppingWithInventory(final Block block) {
            return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(block)
                    .apply(CopyCustomDataFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                        .copy(ITEMS_TAG_NAME, concat(BLOCK_ENTITY_TAG_NAME_IN_ITEM, ITEMS_TAG_NAME))
                        .copy(ENERGY_TAG_NAME, concat(BLOCK_ENTITY_TAG_NAME_IN_ITEM, ENERGY_TAG_NAME))
                    )
                )));
        }

        private static String concat(final String... paths) {
            return String.join(".", paths);
        }
    }
}
