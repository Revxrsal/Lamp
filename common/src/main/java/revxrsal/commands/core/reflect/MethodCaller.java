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
package revxrsal.commands.core.reflect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A high-level wrapper, responsible for invoking methods reflectively.
 */
public interface MethodCaller {

    /**
     * Calls the method of this caller
     *
     * @param instance  Instance to call from. Can be null
     * @param arguments Invoking arguments
     * @return The return result
     */
    Object call(@Nullable Object instance, Object... arguments);

    /**
     * Binds this caller to the specified instance. Calls from the bound method
     * caller will no longer need an instance to call from.
     *
     * @param instance Instance to invoke from. Can be null in case of static
     *                 methods.
     * @return The bound method caller
     */
    default BoundMethodCaller bindTo(@Nullable Object instance) {
        return arguments -> call(instance, arguments);
    }

    /**
     * Represents a {@link MethodCaller} that is attached to an instance
     */
    interface BoundMethodCaller {

        /**
         * Calls the method of this caller
         *
         * @param arguments Invoking arguments
         * @return The return result
         */
        Object call(@NotNull Object... arguments);
    }
}
