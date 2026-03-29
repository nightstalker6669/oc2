/* SPDX-License-Identifier: MIT */

package net.minecraftforge.common.capabilities;

public final class CapabilityManager {
    public static <T> Capability<T> get(final CapabilityToken<T> token) {
        return new Capability<>();
    }

    private CapabilityManager() {
    }
}
