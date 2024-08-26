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
package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Range;

/**
 * Thrown when a numerical parameter that is annotated with {@link Range}
 * gets a value outside its allowed range.
 *
 * @see Range
 */
@ThrowableFromCommand
public class NumberNotInRangeException extends RuntimeException {

    /**
     * The inputted value
     */
    private final @NotNull Number input;

    /**
     * The minimum and maximum values of the range
     */
    private final double minimum, maximum;

    public NumberNotInRangeException(@NotNull Number input, double minimum, double maximum) {
        this.input = input;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * The inputted value
     *
     * @return The inputted value
     */
    public @NotNull Number input() {
        return input;
    }

    /**
     * The minimum value of the range
     *
     * @return The minimum value of the range
     */
    public double minimum() {
        return minimum;
    }

    /**
     * The maximum value of the range
     *
     * @return The maximum value of the range
     */
    public double maximum() {
        return maximum;
    }
}
