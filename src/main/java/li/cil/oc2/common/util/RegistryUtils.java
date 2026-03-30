/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.util;

import li.cil.oc2.api.API;
import li.cil.oc2.common.registry.RegistryView;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public abstract class RegistryUtils {
    private enum Phase {
        PRE_INIT,
        INIT,
        POST_INIT,
    }

    private static final List<DeferredRegister<?>> ENTRIES = new ArrayList<>();
    private static IEventBus eventBus;
    private static Phase phase = Phase.PRE_INIT;

    public static <T> DeferredRegister<T> getInitializerFor(final ResourceKey<? extends Registry<T>> key) {
        if (phase != Phase.INIT) throw new IllegalStateException();

        final DeferredRegister<T> entry = DeferredRegister.create(key, API.MOD_ID);
        ENTRIES.add(entry);
        return entry;
    }

    public static void begin(final IEventBus modEventBus) {
        if (phase != Phase.PRE_INIT) throw new IllegalStateException();
        eventBus = modEventBus;
        phase = Phase.INIT;
    }

    public static void finish() {
        if (phase != Phase.INIT) throw new IllegalStateException();
        phase = Phase.POST_INIT;

        for (final DeferredRegister<?> register : ENTRIES) {
            register.register(Objects.requireNonNull(eventBus));
        }

        ENTRIES.clear();
    }

    public static <T> String key(final RegistryView<T> registry, final T value) {
        return Objects.requireNonNull(registry.getKey(value)).toString();
    }

    public static <T> String key(final DeferredHolder<T, ? extends T> deferredHolder) {
        return deferredHolder.getId().toString();
    }

    public static <T> Optional<String> optionalKey(final RegistryView<T> registry, @Nullable final T value) {
        if (value == null) {
            return Optional.empty();
        }

        final ResourceLocation providerName = registry.getKey(value);
        if (providerName == null) {
            return Optional.empty();
        }

        return Optional.of(providerName.toString());
    }

    public static <T> Optional<String> optionalKey(@Nullable final DeferredHolder<T, ? extends T> deferredHolder) {
        if (deferredHolder == null) {
            return Optional.empty();
        }

        return Optional.of(deferredHolder.getId().toString());
    }

    public static <T> Supplier<RegistryView<T>> makeRegistryView(final DeferredRegister<T> register) {
        register.makeRegistry(builder -> {
        });
        final Supplier<Registry<T>> registrySupplier = register.getRegistry();
        final ResourceKey<? extends Registry<T>> registryKey = register.getRegistryKey();

        return () -> new RegistryView<>() {
            @Override
            public ResourceKey<? extends Registry<T>> getRegistryKey() {
                return registryKey;
            }

            @Override
            public ResourceLocation getKey(@Nullable final T value) {
                if (value == null) {
                    return null;
                }
                return registrySupplier.get().getKey(value);
            }

            @Override
            public T getValue(@Nullable final ResourceLocation location) {
                if (location == null) {
                    return null;
                }
                return registrySupplier.get().get(location);
            }

            @Override
            public Collection<T> getValues() {
                return registrySupplier.get().stream().toList();
            }

            @Override
            public Set<ResourceLocation> getKeys() {
                return registrySupplier.get().keySet();
            }

            @Override
            public Registry<T> unwrap() {
                return registrySupplier.get();
            }
        };
    }

    private RegistryUtils() {
    }
}
