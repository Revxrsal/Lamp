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
package revxrsal.commands.annotation;

import org.jetbrains.annotations.Range;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that marks that collection-based annotation, such as {@link java.util.List},
 * {@link java.util.Set}, or a {@code T[]} array type, should have size
 * in the range of {@link Sized#min()} and {@link Sized#max()} (both inclusive)
 * <p>
 * <em>Note</em>: If a parameter has {@link #min()} == 0, it will automatically be
 * marked as optional.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@NotSender.ImpliesNotSender
public @interface Sized {

    /**
     * The minimum number of entries allowed.
     * <p>
     * A min of 0 implies the parameter to be optional
     *
     * @return The minimum size
     */
    @Range(from = 0, to = Integer.MAX_VALUE) int min() default 0;

    /**
     * The maximum number of entries allowed.
     *
     * @return The maximum size
     */
    @Range(from = 0, to = Integer.MAX_VALUE) int max() default Integer.MAX_VALUE;

}
