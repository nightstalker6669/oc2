/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.util;

import java.lang.ref.WeakReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class LazyValueUtils {
    public static <T, U> void addWeakListener(final LazyValue<T> value, final U weakValue, final BiConsumer<U, LazyValue<T>> listener) {
        value.addListener(buildListener(new WeakReference<>(weakValue), listener));
    }

    private static <T, U> Consumer<LazyValue<T>> buildListener(final WeakReference<U> weakValue, final BiConsumer<U, LazyValue<T>> listener) {
        return invalidatable -> {
            final U value = weakValue.get();
            if (value != null) {
                listener.accept(value, invalidatable);
            }
        };
    }

    private LazyValueUtils() {
    }
}
