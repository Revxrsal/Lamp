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
package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.process.ParameterResolver.ParameterResolverContext;

import java.util.function.Supplier;

/**
 * A resolver for resolving values that are, by default, resolvable through the command
 * invocation context, and do not need any data from the arguments to find the value.
 * An example context resolver is finding the sender's world.
 *
 * @param <T> The resolved type
 */
public interface ContextResolver<T> {

    /**
     * Resolves the value of this resolver
     *
     * @param context The command resolving context.
     * @return The resolved value. May or may not be null.
     */
    T resolve(@NotNull ContextResolverContext context) throws Throwable;

    /**
     * Returns a context resolver that returns a static value. This
     * is a simpler way for adding constant values without having to
     * deal with lambdas.
     *
     * @param value The value to return
     * @param <T>   The value type
     * @return The context resolver
     * @since 1.3.0
     */
    static <T> ContextResolver<T> of(@NotNull T value) {
        return context -> value;
    }

    /**
     * Returns a context resolver that returns a supplier value. This
     * is a simpler way for adding values without having to deal
     * with lambdas.
     *
     * @param value The value supplier
     * @param <T>   The value type
     * @return The context resolver
     * @since 1.3.0
     */
    static <T> ContextResolver<T> of(@NotNull Supplier<T> value) {
        return context -> value.get();
    }

    /**
     * Represents the resolving context of {@link ContextResolver}. This contains
     * all the relevant information about the resolving context.
     */
    interface ContextResolverContext extends ParameterResolverContext {}

}
