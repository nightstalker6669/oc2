/* SPDX-License-Identifier: MIT */

package net.minecraftforge.common.capabilities;

import net.minecraftforge.common.util.LazyOptional;

public final class Capability<T> {
    public <U> LazyOptional<U> orEmpty(final Capability<U> other, final LazyOptional<? extends U> optional) {
        return this == other ? optional.cast() : LazyOptional.empty();
    }
}
