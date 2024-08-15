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
package revxrsal.commands.node.parser;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandFunction;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.reflect.MethodCaller.BoundMethodCaller;
import revxrsal.commands.response.ResponseHandler;
import revxrsal.commands.util.Strings;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@AllArgsConstructor
final class CommandFunctionImpl implements CommandFunction {

    private final @NotNull Lamp<?> lamp;
    private final @NotNull Method method;
    private final @Unmodifiable Map<String, CommandParameter> parameters;
    private final @NotNull AnnotationList annotations;
    private final @NotNull BoundMethodCaller caller;
    private final @NotNull ResponseHandler<?, ?> responseHandler;

    @Override
    public <A extends CommandActor> Lamp<A> lamp() {
        return (Lamp<A>) lamp;
    }

    @Override
    public @NotNull String name() {
        return method.getName();
    }

    @Override
    public @NotNull AnnotationList annotations() {
        return annotations;
    }

    @Override
    public @NotNull Method method() {
        return method;
    }

    @Override
    public @NotNull @Unmodifiable Map<String, CommandParameter> parametersByName() {
        return parameters;
    }

    @Override
    public @NotNull CommandParameter parameter(String name) {
        CommandParameter parameter = parameters.get(name);
        if (parameter == null)
            throw new NoSuchElementException("No such parameter with name '" + name + "'");
        return parameter;
    }

    @Override
    public @NotNull BoundMethodCaller caller() {
        return caller;
    }

    @Override
    public <T> T call(@NotNull Object... arguments) {
        //noinspection unchecked
        return (T) caller.call(arguments);
    }

    @Override
    public @NotNull <T> ResponseHandler<?, T> responseHandler() {
        //noinspection unchecked
        return (ResponseHandler<?, T>) responseHandler;
    }

    public static @NotNull CommandFunction create(
            @NotNull Method method,
            @NotNull AnnotationList annotations,
            @NotNull Lamp<?> lamp,
            @NotNull BoundMethodCaller caller
    ) {
        Parameter[] pArray = method.getParameters();
        Map<String, CommandParameter> parameters = new LinkedHashMap<>(pArray.length);
        for (int methodIndex = 0; methodIndex < pArray.length; methodIndex++) {
            Parameter parameter = pArray[methodIndex];
            AnnotationList parameterAnnotations = AnnotationList.create(parameter)
                    .replaceAnnotations(parameter, lamp.annotationReplacers());
            String name = Strings.getOverriddenName(parameter)
                    .orElseGet(() -> lamp.parameterNamingStrategy().getName(parameter));
            FunctionParameter fnParameter = new FunctionParameter(parameter, name, parameterAnnotations, methodIndex);
            parameters.put(fnParameter.name(), fnParameter);
        }
        parameters = Collections.unmodifiableMap(parameters);
        ResponseHandler<?, Object> handler = lamp.responseHandler(method.getGenericReturnType(), annotations);
        return new CommandFunctionImpl(lamp, method, parameters, annotations, caller, handler);
    }

    @Override
    public String toString() {
        return "CommandFunction(" + method.toGenericString() + ")";
    }
}
