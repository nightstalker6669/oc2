/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.capabilities;

import li.cil.oc2.common.util.LazyValue;
import net.minecraft.core.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface CapabilityProvider {
    @Nonnull
    <T> LazyValue<T> getCapability(CapabilityRef<T> capability, @Nullable Direction side);

    @Nonnull
    default <T> LazyValue<T> getCapability(final CapabilityRef<T> capability) {
        return getCapability(capability, null);
    }
}
