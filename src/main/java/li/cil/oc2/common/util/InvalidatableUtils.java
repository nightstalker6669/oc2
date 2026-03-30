/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.util;

import li.cil.oc2.api.util.Invalidatable;

import java.lang.ref.WeakReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class InvalidatableUtils {
    public static <T> Invalidatable<T> fromLazyValue(final LazyValue<T> value) {
        return value.resolve().map(resolved -> {
            final Invalidatable<T> invalidatable = Invalidatable.of(resolved);
            LazyValueUtils.addWeakListener(value, invalidatable, (wrapped, ignored) -> wrapped.invalidate());
            return invalidatable;
        }).orElseGet(Invalidatable::empty);
    }

    public static <T, U> Invalidatable.ListenerToken addWeakListener(final Invalidatable<T> invalidatable, final U weakValue, final BiConsumer<U, Invalidatable<T>> listener) {
        return invalidatable.addListener(buildListener(new WeakReference<>(weakValue), listener));
    }

    private static <T, U> Consumer<Invalidatable<T>> buildListener(final WeakReference<U> weakValue, final BiConsumer<U, Invalidatable<T>> listener) {
        return invalidatable -> {
            final U value = weakValue.get();
            if (value != null) {
                listener.accept(value, invalidatable);
            }
        };
    }

    private InvalidatableUtils() {
    }
}
