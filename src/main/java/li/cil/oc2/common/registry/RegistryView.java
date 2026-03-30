/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public interface RegistryView<T> {
    ResourceKey<? extends Registry<T>> getRegistryKey();

    @Nullable
    ResourceLocation getKey(@Nullable T value);

    @Nullable
    T getValue(@Nullable ResourceLocation location);

    Collection<T> getValues();

    Set<ResourceLocation> getKeys();

    Registry<T> unwrap();
}
