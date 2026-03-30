/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.util;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.function.Consumer;

public final class ParameterizedEvent<TEventParameter> extends HashSet<Consumer<TEventParameter>> implements Consumer<TEventParameter> {
    private static final long serialVersionUID = 1L;

    @Override
    public void accept(@Nullable final TEventParameter event) {
        for (final Consumer<TEventParameter> listener : this) {
            listener.accept(event);
        }
    }
}
