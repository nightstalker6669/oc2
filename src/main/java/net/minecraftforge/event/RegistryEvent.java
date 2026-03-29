package net.minecraftforge.event;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

public final class RegistryEvent {
    private RegistryEvent() {
    }

    public static class MissingMappings<T> {
        public List<Mapping<T>> getAllMappings() {
            return Collections.emptyList();
        }

        public static class Mapping<T> {
            public ResourceLocation key;

            public void remap(final T value) {
            }
        }
    }
}
