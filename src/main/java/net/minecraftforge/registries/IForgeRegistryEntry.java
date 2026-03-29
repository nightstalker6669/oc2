/* SPDX-License-Identifier: MIT */

package net.minecraftforge.registries;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public interface IForgeRegistryEntry<T> {
    T setRegistryName(ResourceLocation name);

    @Nullable
    ResourceLocation getRegistryName();

    default Class<?> getRegistryType() {
        throw new UnsupportedOperationException();
    }
}
