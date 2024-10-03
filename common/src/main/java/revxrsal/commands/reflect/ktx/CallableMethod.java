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
package revxrsal.commands.reflect.ktx;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.reflect.MethodCaller;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * A utility class that combines a {@link Method} with a {@link MethodCaller}.
 * <p>
 * This class is immutable, therefore is safe to share across multiple
 * threads.
 */
public final class CallableMethod {
    private final @NotNull Method method;
    private final @NotNull MethodCaller caller;

    /**
     *
     */
    public CallableMethod(
            @NotNull Method method,
            @NotNull MethodCaller caller
    ) {
        this.method = method;
        this.caller = caller;
    }

    public @NotNull Method method() {return method;}

    public @NotNull MethodCaller caller() {return caller;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        CallableMethod that = (CallableMethod) obj;
        return Objects.equals(this.method, that.method) &&
                Objects.equals(this.caller, that.caller);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, caller);
    }

    @Override
    public String toString() {
        return "CallableMethod[" +
                "method=" + method + ", " +
                "caller=" + caller + ']';
    }

}
