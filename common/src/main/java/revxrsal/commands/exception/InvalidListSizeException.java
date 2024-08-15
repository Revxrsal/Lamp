/*
 * This file is part of sweeper, licensed under the MIT License.
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
package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Sized;

import java.util.List;

/**
 * Exception thrown when a collection-like type does not meet the specified size constraints
 * set by {@link Sized @Sized}.
 * <p>
 * This exception is used to indicate that the size of a list is either
 * smaller than the minimum allowed size or larger than the maximum allowed size.
 */
public class InvalidListSizeException extends RuntimeException {

    private final int minimum, maximum;
    private final int inputSize;
    private final @NotNull List<Object> items;

    public InvalidListSizeException(int minimum, int maximum, int inputSize, @NotNull List<Object> items) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.inputSize = inputSize;
        this.items = items;
    }

    /**
     * Returns the minimum allowed size of the list.
     *
     * @return the minimum allowed size of the list
     */
    public int minimum() {
        return minimum;
    }

    /**
     * Returns the maximum allowed size of the list.
     *
     * @return the maximum allowed size of the list
     */
    public int maximum() {
        return maximum;
    }

    /**
     * Returns the actual size of the input list.
     *
     * @return the actual size of the input list
     */
    public int inputSize() {
        return inputSize;
    }

    /**
     * Returns the list of items that caused this exception to be thrown.
     *
     * @return the list of items that caused this exception to be thrown
     */
    public <T> @NotNull List<T> items() {
        //noinspection unchecked
        return (List<T>) items;
    }
}
