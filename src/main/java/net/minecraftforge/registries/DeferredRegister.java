/* SPDX-License-Identifier: MIT */

package net.minecraftforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.function.Supplier;

public final class DeferredRegister<T> {
    private final net.neoforged.neoforge.registries.DeferredRegister<T> delegate;

    private DeferredRegister(final net.neoforged.neoforge.registries.DeferredRegister<T> delegate) {
        this.delegate = delegate;
    }

    public static <T> DeferredRegister<T> create(final ResourceKey<? extends Registry<T>> key, final String namespace) {
        return new DeferredRegister<>(net.neoforged.neoforge.registries.DeferredRegister.create(key, namespace));
    }

    public static <T> DeferredRegister<T> create(final IForgeRegistry<T> registry, final String namespace) {
        return create(registry.getRegistryKey(), namespace);
    }

    public <I extends T> RegistryObject<I> register(final String name, final Supplier<? extends I> supplier) {
        return new RegistryObject<>(delegate.register(name, supplier));
    }

    public void register(final IEventBus bus) {
        delegate.register(bus);
    }

    public Supplier<IForgeRegistry<T>> makeRegistry(final Class<T> type, final Supplier<RegistryBuilder<T>> builderFactory) {
        delegate.makeRegistry(builder -> builderFactory.get().applyTo(builder));
        final Supplier<Registry<T>> registrySupplier = delegate.getRegistry();
        final ResourceKey<? extends Registry<T>> registryKey = delegate.getRegistryKey();
        return () -> new NeoBackedForgeRegistry<>(registryKey, registrySupplier.get());
    }

    public Collection<RegistryObject<? extends T>> getEntries() {
        return delegate.getEntries().stream()
            .map(holder -> new RegistryObject<T>(holder))
            .collect(Collectors.toList());
    }

    ResourceKey<? extends Registry<T>> getRegistryKey() {
        return delegate.getRegistryKey();
    }
}
