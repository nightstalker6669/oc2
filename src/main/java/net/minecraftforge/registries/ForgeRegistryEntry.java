/* SPDX-License-Identifier: MIT */

package net.minecraftforge.registries;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class ForgeRegistryEntry<T> implements IForgeRegistryEntry<T> {
    @Nullable private ResourceLocation registryName;

    @Override
    public T setRegistryName(final ResourceLocation name) {
        this.registryName = name;
        return (T) this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }
}
