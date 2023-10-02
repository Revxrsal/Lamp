/*
 * This file is part of Lamp, licensed under the MIT License.
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
package revxrsal.commands.ktx.call;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.core.reflect.MethodCaller;
import revxrsal.commands.util.Preconditions;

import java.lang.reflect.Method;

/**
 * A utility class that combines a {@link Method} with a {@link MethodCaller}.
 * <p>
 * This class is immutable, therefore is safe to share across multiple
 * threads.
 */
@Getter
public final class CallableMethod {

    /**
     * The method wrapped by this class
     */
    private final Method method;

    /**
     * The bound method caller
     */
    private final MethodCaller caller;

    /**
     * Creates a new {@link CallableMethod} that wraps the given method and
     * caller.
     *
     * @param method The method to wrap
     * @param caller The bound method caller
     */
    CallableMethod(@NotNull Method method, @NotNull MethodCaller caller) {
        this.method = method;
        this.caller = caller;
    }

    /**
     * Returns the method wrapped by this callable method
     *
     * @return The underlying method
     */
    public @NotNull Method getMethod() {
        return method;
    }

    /**
     * Creates a new {@link CallableMethod} that wraps the given method and
     * caller.
     *
     * @param method The method to wrap
     * @param caller The bound method caller
     * @return A new {@link CallableMethod}
     */
    public static @NotNull CallableMethod of(@NotNull Method method, @NotNull MethodCaller caller) {
        Preconditions.notNull(method, "method");
        Preconditions.notNull(caller, "caller");
        return new CallableMethod(method, caller);
    }

}
