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

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandFunction;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.context.ErrorContext;
import revxrsal.commands.node.CommandAction;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ContextParameter;
import revxrsal.commands.stream.MutableStringStream;

import java.util.HashMap;
import java.util.Map;

public final class ReflectionAction<A extends CommandActor> implements CommandAction<A> {

    private final CommandFunction function;
    private final Map<Integer, ParameterSupplier<A>> parameters = new HashMap<>();

    public ReflectionAction(CommandFunction function) {
        this.function = function;
    }

    @Override
    public void execute(ExecutionContext<A> context, MutableStringStream input) {
        try {
            Object[] arguments = new Object[function.method().getParameterCount()];
            parameters.forEach((index, parameter) -> {
                arguments[index] = parameter.get(context, input);
            });
            context.resolvedArguments().forEach((parameterName, value) -> {
                int index = function.parameter(parameterName).methodIndex();
                arguments[index] = value;
            });

            Object result = function.call(arguments);
            if (result != null) {
                function.responseHandler().handleResponse(result, (BasicExecutionContext) context);
            }
        } catch (Throwable t) {
            context.lamp().handleException(t, ErrorContext.executingFunction(context));
        }
    }

    void addContextParameter(CommandParameter parameter, ContextParameter<A, ?> contextParameter) {
        parameters.put(parameter.methodIndex(), (context, stream) -> contextParameter.resolve(parameter, stream, context));
    }

    private interface ParameterSupplier<A extends CommandActor> {
        Object get(@NotNull ExecutionContext<A> context, @NotNull MutableStringStream input);
    }
}
