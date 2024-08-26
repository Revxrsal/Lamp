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
package revxrsal.commands.parameter;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;

import java.lang.reflect.Type;

/**
 * A utility class for simplifying the differences between a {@link ParameterType} and a {@link ContextParameter},
 * without having to deal with unnecessary casting or generics
 */
public final class ParameterResolver<A extends CommandActor, T> {

    /**
     * The underlying resolver. This must be either a {@link ContextParameter} or
     * a {@link ParameterType} (but not both)
     */
    private final Object resolver;

    private ParameterResolver(Object resolver) {
        if (resolver instanceof ParameterType && resolver instanceof ContextParameter)
            throw new IllegalArgumentException("A ParameterResolver cannot wrap an object that is both a ParameterType and a ContextParameter.");
        if (!(resolver instanceof ParameterType || resolver instanceof ContextParameter))
            throw new IllegalArgumentException("A ParameterResolver cannot wrap an object that is not a ParameterType or a ContextParameter.");
        this.resolver = resolver;
    }

    public static <A extends CommandActor, T> @NotNull ParameterResolver<A, T> parameterType(@NotNull ParameterType<A, T> type) {
        return new ParameterResolver<>(type);
    }

    public static <A extends CommandActor, T> @NotNull ParameterResolver<A, T> contextParameter(@NotNull ContextParameter<A, T> type) {
        return new ParameterResolver<>(type);
    }

    /**
     * Tests whether this actually consumes input or not.
     *
     * @return true if this is a {@link ParameterType}, false if {@link ContextParameter}.
     */
    public boolean consumesInput() {
        return isParameterType();
    }

    public boolean isParameterType() {
        return resolver instanceof ParameterType;
    }

    public boolean isContextParameter() {
        return resolver instanceof ContextParameter;
    }

    public @NotNull ParameterType<A, T> requireParameterType() {
        return requireParameterType("Expected a ParameterType, received a ContextResolver (resolver: " + resolver + ")");
    }

    public @NotNull ParameterType<A, T> requireParameterType(Type typeHint) {
        return requireParameterType("Expected a ParameterType, received a ContextResolver (resolver: " + resolver + ", type: " + typeHint + ")");
    }

    public @NotNull ContextParameter<A, T> requireContextParameter() {
        return requireContextParameter("Expected a ContextResolver, received a ParameterType (resolver: " + resolver + ")");
    }

    public @NotNull ContextParameter<A, T> requireContextParameter(Type typeHint) {
        return requireContextParameter("Expected a ContextResolver, received a ParameterType (resolver: " + resolver + ", type: " + typeHint + ")");
    }

    public @NotNull ParameterType<A, T> requireParameterType(@NotNull String errorMessage) {
        if (!isParameterType())
            throw new IllegalStateException(errorMessage);
        //noinspection unchecked
        return (ParameterType<A, T>) resolver;
    }

    public @NotNull ContextParameter<A, T> requireContextParameter(@NotNull String errorMessage) {
        if (!isContextParameter())
            throw new IllegalStateException(errorMessage);
        //noinspection unchecked
        return (ContextParameter<A, T>) resolver;
    }
}

