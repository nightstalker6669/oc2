/* SPDX-License-Identifier: MIT */

package net.minecraftforge.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ICapabilityProvider {
    @Nonnull
    <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side);

    @Nonnull
    default <T> LazyOptional<T> getCapability(final Capability<T> capability) {
        return getCapability(capability, null);
    }
}
