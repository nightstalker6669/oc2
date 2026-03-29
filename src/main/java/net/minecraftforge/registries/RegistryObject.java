/* SPDX-License-Identifier: MIT */

package net.minecraftforge.registries;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Optional;
import java.util.function.Supplier;

public final class RegistryObject<T> implements Supplier<T> {
    private final DeferredHolder<?, ?> holder;

    RegistryObject(final DeferredHolder<?, ?> holder) {
        this.holder = holder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) holder.get();
    }

    public ResourceLocation getId() {
        return holder.getId();
    }

    public boolean isPresent() {
        return holder.isBound();
    }

    public Optional<T> asOptional() {
        return isPresent() ? Optional.of(get()) : Optional.empty();
    }
}
