/*
 * This file is part of Lamp, licensed under the MIT License.
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
package revxrsal.commands.ktx.call;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.util.Preconditions;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A simple wrapper around a function declared in Kotlin. This
 * interface simplifies taking advantage of Kotlin's default
 * values for parameters, and allows the use of named
 * parameters.
 * <p>
 * This class is thread-safe, as it is immutable and uses synchronization
 * for lazy reflection fetching.
 */
public interface KotlinFunction {

    /**
     * Generates a {@link KotlinFunction} for the given {@link Method}.
     *
     * @param method The method to wrap
     * @return The wrapper {@link KotlinFunction}
     */
    static @NotNull KotlinFunction wrap(@NotNull Method method) {
        Preconditions.notNull(method, "method");
        return new KotlinFunctionImpl(method);
    }

    /**
     * Tests whether is this function suspend or not
     *
     * @return If this function is suspend or not
     */
    boolean isSuspend();

    /**
     * Calls the function with the given arguments. This will
     * use the order of parameters declared in the original Kotlin
     * function.
     * <p>
     * To use the default value of a parameter, pass a {@code null} in its place.
     *
     * @param <T>        The function return type
     * @param instance   Instance to call the function with
     * @param arguments  The arguments to invoke with
     * @param isOptional A function that guides this invocation
     *                   into knowing which parameters are optional,
     *                   so it can use their default values, and which
     *                   ones are not, so it can report errors as appropriate
     *                   when missing.
     *                   <p>
     *                   This should either test for a certain annotation
     *                   if you have access to the source function, or check against
     *                   a pre-defined list of parameter names assembled by the
     *                   developer, or any other strategy that correctly
     *                   reports whether a parameter is optional or not.
     *                   <p>
     *                   This parameter is necessary as it allows us to drop
     *                   the dependency on kotlin-reflect.
     * @return The function return value
     */
    <T> T call(
            @Nullable Object instance,
            @NotNull List<Object> arguments,
            @NotNull Function<Parameter, Boolean> isOptional
    );

    /**
     * Calls the function with the given arguments, mapped by the index
     * of each parameter. Indexing starts from zero.
     * <p>
     * For example, if a function has three parameters, where the second
     * is optional, it can be invoked with
     * <pre>
     *     callByIndices(map(
     *          0, "value 1", // first parameter
     *          2, "value 3" // third parameter
     *     ))
     * </pre>
     * in which the second parameter will use its default value.
     *
     * @param instance   Instance to call the function with
     * @param arguments  The arguments to invoke with
     * @param isOptional A function that guides this invocation
     *                   into knowing which parameters are optional,
     *                   so it can use their default values, and which
     *                   ones are not, so it can report errors as appropriate
     *                   when missing.
     *                   <p>
     *                   This should either test for a certain annotation
     *                   if you have access to the source function, or check against
     *                   a pre-defined list of parameter names assembled by the
     *                   developer, or any other strategy that correctly
     *                   reports whether a parameter is optional or not.
     *                   <p>
     *                   This parameter is necessary as it allows us to drop
     *                   the dependency on kotlin-reflect.
     * @param <T>        The function return type
     * @return The function return value
     */
    <T> T callByIndices(
            @Nullable Object instance,
            @NotNull Map<Integer, Object> arguments,
            @NotNull Function<Parameter, Boolean> isOptional
    );

    /**
     * Calls the function with the given arguments, mapped by
     * the {@link Parameter} object representing each parameter.
     * <p>
     * This allows flexibility in controlling which parameters to specify,
     * without having to rely on names or indexes. See {@link #getParameters()}
     * and {@link #getParametersByName()}.
     *
     * @param instance   Instance to call the function with
     * @param arguments  The arguments to invoke with
     * @param isOptional A function that guides this invocation
     *                   into knowing which parameters are optional,
     *                   so it can use their default values, and which
     *                   ones are not, so it can report errors as appropriate
     *                   when missing.
     *                   <p>
     *                   This should either test for a certain annotation
     *                   if you have access to the source function, or check against
     *                   a pre-defined list of parameter names assembled by the
     *                   developer, or any other strategy that correctly
     *                   reports whether a parameter is optional or not.
     *                   <p>
     *                   This parameter is necessary as it allows us to drop
     *                   the dependency on kotlin-reflect.
     * @param <T>        The function return type
     * @return The function return value
     */
    <T> T callByParameters(
            @Nullable Object instance,
            @NotNull Map<Parameter, Object> arguments,
            @NotNull Function<Parameter, Boolean> isOptional
    );

    /**
     * Calls the function with the given arguments, mapped by the name
     * of each parameter.
     * <p>
     * <strong>Note</strong>: Parameter names at runtime may not necessarily match
     * the ones at compile-time, in which cases, the function will throw
     * an exception if an invalid name was provided.
     *
     * @param instance   Instance to call the function with
     * @param arguments  The arguments to invoke with
     * @param isOptional A function that guides this invocation
     *                   into knowing which parameters are optional,
     *                   so it can use their default values, and which
     *                   ones are not, so it can report errors as appropriate
     *                   when missing.
     *                   <p>
     *                   This should either test for a certain annotation
     *                   if you have access to the source function, or check against
     *                   a pre-defined list of parameter names assembled by the
     *                   developer, or any other strategy that correctly
     *                   reports whether a parameter is optional or not.
     *                   <p>
     *                   This parameter is necessary as it allows us to drop
     *                   the dependency on kotlin-reflect.
     * @param <T>        The function return type
     * @return The function return value
     */
    <T> T callByNames(
            @Nullable Object instance,
            @NotNull Map<String, Object> arguments,
            @NotNull Function<Parameter, Boolean> isOptional
    );

    /**
     * Returns the method that this function wraps
     *
     * @return The main method
     */
    @NotNull CallableMethod getMethod();

    /**
     * Returns the synthetic method generated by the Kotlin compiler
     * for invoking a function with the default values. This may be
     * null for methods which contain no default values.
     * <p>
     * Note that the synthetic method is searched for on demand only.
     * This function will attempt to find one on the first call.
     *
     * @return The synthetic method
     */
    @Nullable CallableMethod getDefaultSyntheticMethod();

    /**
     * Returns the real function parameters, as declared in the Kotlin source.
     * <p>
     * This will not include the leading parameter in {@code object} functions,
     * which is generated by the compiler.
     * <p>
     * This list is unmodifiable.
     *
     * @return The function parameters
     */
    @Unmodifiable @NotNull List<Parameter> getParameters();

    /**
     * Returns the real function parameters and their names, as declared
     * in the Kotlin source.
     * <p>
     * This will not include the leading parameter in {@code object} functions,
     * which is generated by the compiler.
     * <p>
     * This list is unmodifiable.
     *
     * @return The function parameters
     */
    @Unmodifiable @NotNull Map<String, Parameter> getParametersByName();

    /**
     * Returns the parameter by the given name, otherwise throws
     * an {@link IllegalArgumentException}
     *
     * @param name The parameter name.
     * @return The parameter
     */
    @NotNull Parameter getParameter(@NotNull String name);

    /**
     * Returns the parameter by the given index, otherwise throws
     * an {@link IndexOutOfBoundsException}
     *
     * @param index The parameter index.
     * @return The parameter
     * @throws IndexOutOfBoundsException if out of bounds
     */
    @NotNull Parameter getParameter(int index) throws IndexOutOfBoundsException;

}
