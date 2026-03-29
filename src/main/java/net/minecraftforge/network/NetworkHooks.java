/* SPDX-License-Identifier: MIT */

package net.minecraftforge.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

import java.util.OptionalInt;
import java.util.function.Consumer;

public final class NetworkHooks {
    private NetworkHooks() {
    }

    public static OptionalInt openGui(final ServerPlayer player, final MenuProvider menuProvider, final BlockPos pos) {
        return player.openMenu(menuProvider, pos);
    }

    public static OptionalInt openGui(final ServerPlayer player, final MenuProvider menuProvider, final Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        return player.openMenu(menuProvider, extraDataWriter);
    }
}
