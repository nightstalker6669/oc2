/* SPDX-License-Identifier: MIT */

package net.minecraftforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public final class ForgeRegistries {
    public static final IForgeRegistry<Block> BLOCKS = builtIn(Registries.BLOCK);
    public static final IForgeRegistry<Item> ITEMS = builtIn(Registries.ITEM);
    public static final IForgeRegistry<BlockEntityType<?>> BLOCK_ENTITIES = builtIn(Registries.BLOCK_ENTITY_TYPE);
    public static final IForgeRegistry<EntityType<?>> ENTITIES = builtIn(Registries.ENTITY_TYPE);
    public static final IForgeRegistry<MenuType<?>> CONTAINERS = builtIn(Registries.MENU);
    public static final IForgeRegistry<RecipeSerializer<?>> RECIPE_SERIALIZERS = builtIn(Registries.RECIPE_SERIALIZER);
    public static final IForgeRegistry<SoundEvent> SOUND_EVENTS = builtIn(Registries.SOUND_EVENT);

    private static <T> IForgeRegistry<T> builtIn(final ResourceKey<? extends Registry<T>> key) {
        return new NeoBackedForgeRegistry<>(key, lookup(key));
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> lookup(final ResourceKey<? extends Registry<T>> key) {
        return (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
    }

    private ForgeRegistries() {
    }
}
