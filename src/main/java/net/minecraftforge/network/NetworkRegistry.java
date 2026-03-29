package net.minecraftforge.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Predicate;
import java.util.function.Supplier;

public final class NetworkRegistry {
    private NetworkRegistry() {
    }

    public static SimpleChannel newSimpleChannel(final ResourceLocation name, final Supplier<String> protocolVersion, final Predicate<String> clientAcceptedVersions, final Predicate<String> serverAcceptedVersions) {
        return new SimpleChannel(name, protocolVersion);
    }
}
