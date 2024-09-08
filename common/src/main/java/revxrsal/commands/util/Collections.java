/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
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

import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static revxrsal.commands.util.Preconditions.cannotInstantiate;

/**
 * Provides utilities for dealing with collections and iterators. This
 * provides similar functions to {@link java.util.stream.Stream}s without
 * the intermediate layer of a Stream.
 * <p>
 * This improves performance and allows us to make use of invariants of data-backed
 * collections (as a Stream is not necessarily backed by a data structure).
 */
public final class Collections {

    private Collections() {
        cannotInstantiate(Collections.class);
    }

    /**
     * Applies a mapping function to a given map's keys, and returns
     * a new map that contains the mapped keys to their values
     *
     * @param map   Original map
     * @param remap Remapping function
     * @param <K>   Old key type
     * @param <L>   New key type
     * @param <V>   Value type
     * @return The new remapped map
     */
    @Contract(pure = true)
    @CheckReturnValue
    public static <L, K, V> Map<L, V> mapKeys(Map<K, V> map, Function<K, L> remap) {
        Map<L, V> remapped = new HashMap<>();
        for (Map.Entry<K, V> e : map.entrySet()) {
            if (remapped.put(
                    remap.apply(e.getKey()), // Remap here
                    e.getValue()
            ) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }
        return remapped;
    }

    /**
     * Returns an element at the given [index] or `null` if the [index] is out of bounds of this array.
     */
    public static <T> T getOrNull(T[] array, int index) {
        return (index >= 0 && index <= lastIndex(array)) ? array[index] : null;
    }

    /**
     * Returns an element at the given [index] or `null` if the [index] is out of bounds of this array.
     */
    public static <T> T getOrNull(List<T> list, int index) {
        return (index >= 0 && index <= list.size() - 1) ? list.get(index) : null;
    }

    /**
     * Returns the index of the last element in the array
     *
     * @param array Array to get for
     * @return The last index
     */
    private static int lastIndex(Object[] array) {
        return array.length - 1;
    }

    /**
     * Creates a grown copy of the given array, with the given element
     * inserted at the beginning.
     * <p>
     * Note: The given array is not modified.
     *
     * @param original Original array
     * @param item     The item to insert
     * @param <T>      Array type
     * @return A new array consisting of the new item + original elements
     */
    @Contract(pure = true, value = "null, _ -> fail; _, _ -> new")
    @CheckReturnValue
    public static <T> Object[] insertAtBeginning(
            @NotNull T[] original,
            @Nullable T item
    ) {
        Preconditions.notNull(original, "original array");
        int newSize = original.length + 1;
        Object[] newArr = new Object[newSize];
        newArr[0] = item;
        System.arraycopy(original, 0, newArr, 1, original.length);
        return newArr;
    }

    public static @NotNull <T> T first(@NotNull Iterable<T> iterator, @NotNull Predicate<T> predicate) {
        for (T t : iterator) {
            if (predicate.test(t))
                return t;
        }
        throw new IllegalStateException("No element found matching the predicate");
    }

    public static @NotNull <T> List<T> filter(@NotNull Iterable<T> iterator, @NotNull Predicate<T> predicate) {
        List<T> list = new ArrayList<>();
        for (T t : iterator) {
            if (predicate.test(t))
                list.add(t);
        }
        return list;
    }

    public static @NotNull <U, T> List<T> map(@NotNull Iterable<U> iterator, @NotNull Function<U, T> fn) {
        List<T> list = new ArrayList<>();
        for (U u : iterator) {
            list.add(fn.apply(u));
        }
        return list;
    }

    public static @NotNull <U, T> LinkedList<T> mapToLinkedList(@NotNull Iterable<U> iterator, @NotNull Function<U, T> fn) {
        LinkedList<T> list = new LinkedList<>();
        for (U u : iterator) {
            list.add(fn.apply(u));
        }
        return list;
    }

    public static <E> @NotNull @UnmodifiableView Iterator<E> unmodifiableIterator(@NotNull Iterator<E> iterator) {
        return new UnmodifiableIterator<>(iterator);
    }

    /**
     * UnmodifiableIterator, A wrapper around an iterator instance that
     * disables the remove method.
     */
    static final class UnmodifiableIterator<E> implements Iterator<E> {

        /**
         * iterator, The base iterator.
         */
        private final Iterator<? extends E> iterator;

        private UnmodifiableIterator(final Iterator<? extends E> iterator) {
            this.iterator = iterator;
        }

        public static <E> Iterator<E> create(final Iterator<? extends E> iterator) {
            if (iterator == null) {
                throw new NullPointerException("The iterator can not be null.");
            }
            return new UnmodifiableIterator<>(iterator);
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Iterator.remove() is disabled.");
        }
    }
}
