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
package revxrsal.commands.reflect.ktx;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.reflect.MethodCaller;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.reflect.Modifier.isStatic;
import static revxrsal.commands.reflect.ktx.DefaultFunctionFinder.findDefaultFunction;
import static revxrsal.commands.reflect.ktx.KotlinConstants.continuation;
import static revxrsal.commands.reflect.ktx.KotlinConstants.defaultPrimitiveValue;
import static revxrsal.commands.reflect.ktx.KotlinSingletons.getCallerForNonDefault;
import static revxrsal.commands.util.Collections.getOrNull;
import static revxrsal.commands.util.Collections.mapKeys;
import static revxrsal.commands.util.Lazy.of;

final class KotlinFunctionImpl implements KotlinFunction {

    private static final String CONTEXT_PARAMETER_PREFIX = "$this$";

    private final CallableMethod mainMethod;
    private final Supplier<@Nullable CallableMethod> defaultMethod;
    private final @Unmodifiable List<Parameter> parameters;
    private final Supplier<@Unmodifiable Map<String, Parameter>> byName = of(() -> {
        Map<String, Parameter> byName = new HashMap<>();
        for (Parameter parameter : getParameters())
            byName.put(parameter.getName(), parameter);
        return Collections.unmodifiableMap(byName);
    });

    public KotlinFunctionImpl(Method mainMethod) {
        MethodCaller mainCaller = getCallerForNonDefault(mainMethod);
        this.parameters = Arrays.asList(mainMethod.getParameters());
        this.mainMethod = new CallableMethod(mainMethod, mainCaller);
        this.defaultMethod = of(() -> findDefaultFunction(mainMethod));
    }

    private static void checkCallableStatic(@Nullable Object instance, @NotNull Method method) {
        if (instance == null && !isStatic(method.getModifiers()))
            throw new IllegalArgumentException("The given method is not static, and no instance was provided. "
                    + "Either mark the function as static with @JvmStatic, or pass the object/companion object value for the instance.");
    }

    @Override
    public @NotNull CallableMethod getMethod() {
        return mainMethod;
    }

    @Override
    public @Nullable CallableMethod getDefaultSyntheticMethod() {
        return defaultMethod.get();
    }

    @Override
    public boolean isSuspend() {
        Parameter lastParameter = getOrNull(parameters, parameters.size() - 1);
        return lastParameter != null && lastParameter.getType() == continuation();
    }

    @Override
    public <T> T call(
            @Nullable Object instance,
            @NotNull List<Object> arguments,
            @NotNull Function<Parameter, Boolean> isOptional
    ) {
        Map<Parameter, Object> callArgs = mapArgsToParams(i -> getOrNull(arguments, i));
        return callByParameters(instance, callArgs, isOptional);
    }

    @Override
    public <T> T callByIndices(
            @Nullable Object instance,
            @NotNull Map<Integer, Object> arguments,
            @NotNull Function<Parameter, Boolean> isOptional
    ) {
        Map<Parameter, Object> callArgs = mapArgsToParams(arguments::get);
        return callByParameters(instance, callArgs, isOptional);
    }

    @Override
    public <T> T callByNames(
            @Nullable Object instance,
            @NotNull Map<String, Object> arguments,
            @NotNull Function<Parameter, Boolean> isOptional
    ) {
        return callByParameters(instance, mapKeys(arguments, this::getParameter), isOptional);
    }

    // Re-adapted from KCallableImpl.callBy
    @Override
    public <T> T callByParameters(
            @Nullable Object instance,
            @NotNull Map<Parameter, Object> arguments,
            @NotNull Function<Parameter, Boolean> isOptional
    ) {
        checkCallableStatic(instance, mainMethod.method());
        List<Object> args = new ArrayList<>();
        int mask = 0;
        List<Integer> masks = new ArrayList<>(1);
        int index = 0;

        boolean anyOptional = false;
        boolean hasContextReceiver = false;

        for (Parameter parameter : parameters) {
            if (index == 0 && parameter.getName().startsWith(CONTEXT_PARAMETER_PREFIX)) {
                hasContextReceiver = true;
            }
            if (index != 0 && index % Integer.SIZE == 0) {
                masks.add(mask);
                mask = 0;
            }
            @Nullable Object providedArg = arguments.get(parameter);

            if (providedArg != null) {
                args.add(providedArg);
            }

            // Parameter is not present

            else if (isOptional.apply(parameter)) {
                mask = mask | 1 << index % 32;
                args.add(defaultPrimitiveValue(parameter.getType()));
                anyOptional = true;
            } else if (parameter.isVarArgs()) {
                args.add(Array.newInstance(parameter.getType(), 0));
            } else {
                throw new IllegalArgumentException("No argument provided for a required parameter: " + parameter + ".");
            }
            index++;
        }

        if (!anyOptional)
            //noinspection unchecked
            return (T) mainMethod.caller().call(instance, args.toArray());

        CallableMethod defaultMethod = this.defaultMethod.get();

        if (defaultMethod == null) {
            // there's a possibility that our isOptional checker failed.
            if (mainMethod.method().getParameterCount() == args.size())
                //noinspection unchecked
                return (T) mainMethod.caller().call(instance, args.toArray());
            throw new IllegalArgumentException("Unable to invoke function with default parameters. " +
                    "This may happen because you have an @Optional non-null primitive type (e.g. Int) " +
                    "with no default value using @Default or a Kotlin-default value. " +
                    "It may also occur if you have @Switch with no default value. (@Switch param: Boolean = ...). " +
                    "Either mark it as nullable, add a default value (@Optional param: Type = ...), or use @Default");
        }

        masks.add(mask);
        if (hasContextReceiver) {
            masks.set(0, masks.get(0) / 2);
        }
        args.addAll(masks);
        // DefaultConstructorMarker or MethodHandle
        args.add(null);

        //noinspection unchecked
        return (T) defaultMethod.caller().call(instance, args.toArray());
    }

    @NotNull
    private Map<Parameter, Object> mapArgsToParams(@NotNull Function<Integer, Object> map) {
        Map<Parameter, Object> callArgs = new HashMap<>();
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            callArgs.put(parameter, map.apply(i));
        }
        return callArgs;
    }

    @Override
    public @Unmodifiable @NotNull List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public @Unmodifiable @NotNull Map<String, Parameter> getParametersByName() {
        return byName.get();
    }

    @Override
    public @NotNull Parameter getParameter(@NotNull String name) {
        Parameter parameter = getParametersByName().get(name);
        if (parameter == null)
            throw new IllegalArgumentException("No such parameter: '" + name + "'. Available parameters: " + getParametersByName().keySet());
        return parameter;
    }

    @Override
    public @NotNull Parameter getParameter(int index) {
        return parameters.get(index);
    }
}
