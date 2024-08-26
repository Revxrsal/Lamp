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

import static revxrsal.commands.util.Classes.wrap;
import static revxrsal.commands.util.Preconditions.cannotInstantiate;

public final class Numbers {

    private Numbers() {
        cannotInstantiate(Numbers.class);
    }

    public static @NotNull Number getMinValue(@NotNull Class<?> type) {
        type = wrap(type);

        if (type == Byte.class) {
            return Byte.MIN_VALUE;
        } else if (type == Short.class) {
            return Short.MIN_VALUE;
        } else if (type == Integer.class) {
            return Integer.MIN_VALUE;
        } else if (type == Long.class) {
            return Long.MIN_VALUE;
        } else if (type == Float.class) {
            return Float.MIN_VALUE;
        } else if (type == Double.class) {
            return Double.MIN_VALUE;
        }

        throw new IllegalArgumentException("Unsupported type: " + type.getName());
    }

    public static @NotNull Number getMaxValue(@NotNull Class<?> type) {
        type = wrap(type);

        if (type == Byte.class) {
            return Byte.MAX_VALUE;
        } else if (type == Short.class) {
            return Short.MAX_VALUE;
        } else if (type == Integer.class) {
            return Integer.MAX_VALUE;
        } else if (type == Long.class) {
            return Long.MAX_VALUE;
        } else if (type == Float.class) {
            return Float.MAX_VALUE;
        } else if (type == Double.class) {
            return Double.MAX_VALUE;
        }

        throw new IllegalArgumentException("Unsupported type: " + type.getName());
    }

    public static boolean isDecimal(@NotNull Class<?> type) {
        type = wrap(type);
        return type == Float.class || type == Double.class;
    }

}
