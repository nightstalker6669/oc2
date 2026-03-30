/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class LazyValue<T> {
    private static final LazyValue<?> EMPTY = new LazyValue<>(() -> null);

    private final Supplier<? extends T> supplier;
    private final List<Consumer<LazyValue<T>>> listeners = new ArrayList<>();
    private boolean resolved;
    private boolean valid = true;
    @Nullable private T value;

    private LazyValue(final Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    @SuppressWarnings("unchecked")
    public static <T> LazyValue<T> empty() {
        return (LazyValue<T>) EMPTY;
    }

    public static <T> LazyValue<T> of(final Supplier<? extends T> supplier) {
        return new LazyValue<>(supplier);
    }

    public boolean isPresent() {
        return valid && getValue().isPresent();
    }

    public void ifPresent(final Consumer<? super T> consumer) {
        getValue().ifPresent(consumer);
    }

    public <U> Optional<U> map(final Function<? super T, ? extends U> mapper) {
        return getValue().map(mapper);
    }

    public T orElse(final T fallback) {
        return getValue().orElse(fallback);
    }

    public <X extends Throwable> T orElseThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return getValue().orElseThrow(exceptionSupplier);
    }

    public Optional<T> resolve() {
        return getValue();
    }

    @SuppressWarnings("unchecked")
    public <U> LazyValue<U> cast() {
        return (LazyValue<U>) this;
    }

    public void invalidate() {
        if (!valid) {
            return;
        }

        valid = false;
        for (final Consumer<LazyValue<T>> listener : List.copyOf(listeners)) {
            listener.accept(this);
        }
        listeners.clear();
    }

    public void addListener(final Consumer<LazyValue<T>> listener) {
        if (!valid) {
            listener.accept(this);
            return;
        }

        listeners.add(listener);
    }

    private Optional<T> getValue() {
        if (!valid) {
            return Optional.empty();
        }

        if (!resolved) {
            value = supplier.get();
            resolved = true;
        }

        return Optional.ofNullable(value);
    }
}
