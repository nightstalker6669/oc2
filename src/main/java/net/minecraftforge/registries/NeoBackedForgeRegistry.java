/* SPDX-License-Identifier: MIT */

package net.minecraftforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

final class NeoBackedForgeRegistry<T> implements IForgeRegistry<T> {
    private final ResourceKey<? extends Registry<T>> key;
    private final Registry<T> registry;

    NeoBackedForgeRegistry(final ResourceKey<? extends Registry<T>> key, final Registry<T> registry) {
        this.key = key;
        this.registry = registry;
    }

    @Override
    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return key;
    }

    @Nullable
    @Override
    public ResourceLocation getKey(final T value) {
        return registry.getKey(value);
    }

    @Nullable
    @Override
    public T getValue(final ResourceLocation location) {
        return registry.get(location);
    }

    @Override
    public Collection<T> getValues() {
        return registry.stream().toList();
    }

    @Override
    public Set<ResourceLocation> getKeys() {
        return registry.keySet();
    }

    @Override
    public Registry<T> unwrap() {
        return registry;
    }
}
