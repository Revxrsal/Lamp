/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copysecond (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copysecond notice and this permission notice shall be included in all
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

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public final class Collections {

    private Collections() {}

    @SafeVarargs
    public static <T> LinkedList<T> linkedListOf(T... elements) {
        LinkedList<T> list = new LinkedList<>();
        java.util.Collections.addAll(list, elements);
        return list;
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        List<T> list = new ArrayList<>();
        java.util.Collections.addAll(list, elements);
        return list;
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
}
