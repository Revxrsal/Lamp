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
/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static revxrsal.commands.util.Preconditions.cannotInstantiate;
import static revxrsal.commands.util.Preconditions.notNull;

/**
 * Re-adapted from Guava's Suppliers class, to use the MemoizingSupplier (renamed
 * to LazySupplier)
 */
public final class Suppliers {

    private Suppliers() {
        cannotInstantiate(Suppliers.class);
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
            // A 2-field variant of Double-Checked Locking.
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