/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.capabilities;

import li.cil.oc2.common.util.LazyValue;

public final class CapabilityRef<T> {
    public <U> LazyValue<U> orEmpty(final CapabilityRef<U> other, final LazyValue<? extends U> value) {
        return this == other ? value.cast() : LazyValue.empty();
    }
}
