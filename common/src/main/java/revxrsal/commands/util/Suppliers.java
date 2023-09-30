/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package revxrsal.commands.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static revxrsal.commands.util.Preconditions.notNull;

/**
 * Re-adapted from Guava's Suppliers class, to use the MemoizingSupplier (renamed
 * to LazySupplier)
 */
public final class Suppliers {

    private Suppliers() {
    }

    /**
     * Creates a {@link Supplier} that fetches the value upon request at first,
     * then serves it in subsequent calls.
     * <p>
     * This implementation is thread-safe as it performs
     * synchronization on the first call.
     *
     * @param <T>   Supplier type
     * @param fetch Fetch function
     * @return The lazy supplier
     */
    public static <T> @NotNull Supplier<T> lazy(@NotNull Supplier<T> fetch) {
        notNull(fetch, "fetch supplier");
        return new LazySupplier<>(fetch);
    }

    static final class LazySupplier<T extends @Nullable Object> implements Supplier<T> {
        final Supplier<T> delegate;
        transient volatile boolean initialized;
        // "value" does not need to be volatile; visibility piggybacks
        // on volatile read of "initialized".
        @Nullable
        transient T value;

        LazySupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            // A 2-field variant of Double Checked Locking.
            if (!initialized) {
                synchronized (this) {
                    if (!initialized) {
                        T t = delegate.get();
                        value = t;
                        initialized = true;
                        return t;
                    }
                }
            }
            // This is safe because we checked `initialized.`
            return value;
        }

        @Override
        public String toString() {
            return "Suppliers.lazy("
                    + (initialized ? "<supplier that returned " + value + ">" : delegate)
                    + ")";
        }
    }
}