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
package revxrsal.commands.core.reflect;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.ktx.call.KotlinFunction;

import java.lang.reflect.Method;

/**
 * Factory for creating {@link MethodCaller}s for methods.
 */
public interface MethodCallerFactory {

    /**
     * Creates a new {@link MethodCaller} for the specified method.
     *
     * @param method Method to create for
     * @return The reflective method caller
     * @throws Throwable Any exceptions during creation
     */
    @NotNull MethodCaller createFor(@NotNull Method method) throws Throwable;

    /**
     * Returns a {@link MethodCallerFactory} that uses the new
     * method handles API to create method callers.
     *
     * @return The default method caller factory.
     */
    static @NotNull MethodCallerFactory methodHandles() {
        return MethodHandlesCallerFactory.INSTANCE;
    }

    /**
     * Returns a {@link MethodCallerFactory} that allows invocation
     * of Kotlin functions with their default values.
     *
     * @return The Kotlin method caller factory
     */
    static @NotNull MethodCallerFactory kotlinFunctions() {
        return KotlinMethodCallerFactory.INSTANCE;
    }

    /**
     * Returns the default {@link MethodCallerFactory}, which uses
     * the method handles API to create method callers, and
     * {@link KotlinFunction} to call Kotlin methods.
     *
     * @return The default method caller factory.
     */
    static @NotNull MethodCallerFactory defaultFactory() {
        return DefaultMethodCallerFactory.INSTANCE;
    }

}
