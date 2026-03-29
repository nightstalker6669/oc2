/* SPDX-License-Identifier: MIT */

package net.minecraftforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public interface IForgeRegistry<T> {
    ResourceKey<? extends Registry<T>> getRegistryKey();

    @Nullable
    ResourceLocation getKey(T value);

    @Nullable
    T getValue(ResourceLocation location);

    Collection<T> getValues();

    Set<ResourceLocation> getKeys();

    Registry<T> unwrap();
}
