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

import revxrsal.commands.parameter.PrioritySpec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an explicit priority for a certain method over other
 * methods that similarly define a priority in the same class.
 * <p>
 * If a method has higher priority (i.e. lower {@link #value() value}), it will
 * be registered and tested out before the lower-priority one. This may
 * be necessary when certain methods contain overlapping parameters that may both
 * work with the same input, and it is infeasible to define a {@link PrioritySpec}
 * for each of them.
 * <p>
 * Note that this annotation is <em>only</em> considered when two methods
 * both define a {@link CommandPriority}. It is meaningless to compare a method
 * with a priority with a method that has no priority.
 * <p>
 * If you would like to make a command universally of low priority,
 * use {@link Low}
 *
 * @see PrioritySpec
 * @see Low
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandPriority {

    /**
     * The priority value
     *
     * @return The priority
     */
    int value();

    /**
     * Represents a command with a low priority. Unlike {@link CommandPriority}, this
     * will always be lower than any other command, regardless of whether the other
     * command has a priority or not.
     * <p>
     * Two commands with {@link Low @Low} priority will be of equal priority.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Low {}

}
