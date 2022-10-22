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

import java.util.Collection;
import java.util.Objects;

public final class Preconditions {

    private Preconditions() {}

    public static <T> void notEmpty(T[] array, String err) {
        if (array.length == 0)
            throw new IllegalStateException(err);
    }

    public static <T> void notEmpty(Collection<T> collection, String err) {
        if (collection.size() == 0)
            throw new IllegalStateException(err);
    }

    public static <T> void notEmpty(String s, String err) {
        if (s.length() == 0)
            throw new IllegalStateException(err);
    }

    public static void checkArgument(boolean expr, String err) {
        if (!expr)
            throw new IllegalArgumentException(err);
    }

    public static int coerceIn(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }

    public static int coerceAtMost(int value, int max) {
        return Math.min(value, max);
    }

    public static int coerceAtLeast(int value, int min) {
        return Math.max(value, min);
    }

    public static <T> T notNull(T t, String err) {
        return Objects.requireNonNull(t, err + " cannot be null!");
    }

}
