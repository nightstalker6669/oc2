/* SPDX-License-Identifier: MIT */

package net.minecraftforge.common.util;

@FunctionalInterface
public interface NonNullConsumer<T> {
    void accept(T value);
}
