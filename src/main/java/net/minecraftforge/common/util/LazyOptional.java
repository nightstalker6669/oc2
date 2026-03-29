/* SPDX-License-Identifier: MIT */

package net.minecraftforge.common.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class LazyOptional<T> {
    private static final LazyOptional<?> EMPTY = new LazyOptional<>(() -> null);

    private final Supplier<? extends T> supplier;
    private final List<NonNullConsumer<LazyOptional<T>>> listeners = new ArrayList<>();
    private boolean resolved;
    private boolean valid = true;
    @Nullable private T value;

    private LazyOptional(final Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    @SuppressWarnings("unchecked")
    public static <T> LazyOptional<T> empty() {
        return (LazyOptional<T>) EMPTY;
    }

    public static <T> LazyOptional<T> of(final Supplier<? extends T> supplier) {
        return new LazyOptional<>(supplier);
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
    public <U> LazyOptional<U> cast() {
        return (LazyOptional<U>) this;
    }

    public void invalidate() {
        if (!valid) {
            return;
        }

        valid = false;
        for (final NonNullConsumer<LazyOptional<T>> listener : List.copyOf(listeners)) {
            listener.accept(this);
        }
        listeners.clear();
    }

    public void addListener(final NonNullConsumer<LazyOptional<T>> listener) {
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
