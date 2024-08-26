/*
 * This file is part of sweeper, licensed under the MIT License.
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
package revxrsal.commands.exception;

import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandFunction;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.context.ErrorContext;
import revxrsal.commands.exception.context.ErrorContext.ParsingLiteral;
import revxrsal.commands.exception.context.ErrorContext.ParsingParameter;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.node.LiteralNode;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.util.Reflections;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that allows to easily handle individual exceptions
 * by creating smaller functions that handle specific types of exceptions
 * or deal with specific {@link ErrorContext}s.
 * <p>
 * This works by creating a function and annotating it with {@link HandleException}.
 * The function parameters dictate the conditions that this function works in.
 * <p>
 * <ul>
 *     <li>
 *         If a function has no parameters, it will always be invoked
 *     </li>
 *     <li>
 *         If a function has a specific {@link Throwable} type, it will handle exceptions
 *         of such type
 *     </li>
 *     <li>
 *         If a function has a specific {@link ErrorContext} type, it will handle
 *         exceptions of such context
 *     </li>
 *     <li>
 *         If a function has a {@link CommandParameter} parameter, it will handle error
 *         contexts of type {@link ParsingParameter} and automatically get the parameter
 *     </li>
 *     <li>
 *         If a function has a {@link ParameterNode} parameter, it will handle error
 *         contexts of type {@link ParsingParameter} and automatically get the parameter node
 *     </li>
 *     <li>
 *         If a function has a {@link LiteralNode} parameter, it will handle error
 *         contexts of type {@link ParsingLiteral} and automatically get the literal node
 *     </li>
 * </ul>
 * <p>
 * The function will also get these parameters automatically:
 * <ul>
 *     <li>{@link Lamp}</li>
 *     <li>{@link CommandActor}</li>
 *     <li>{@link ExecutionContext}</li>
 *     <li>{@link ErrorContext}</li>
 *     <li>{@link ExecutableCommand}</li>
 *     <li>{@link CommandFunction}</li>
 * </ul>
 *
 * @param <A> The actor type
 */
public class RuntimeExceptionAdapter<A extends CommandActor> implements CommandExceptionHandler<A> {

    private final List<CommandExceptionHandler<A>> handlers = new ArrayList<>();

    /**
     * Registers all {@link HandleException}-annotated methods to this
     * exception handler
     */
    public RuntimeExceptionAdapter() {
        for (Method method : Reflections.getAllMethods(getClass())) {
            if (!isHandler(method))
                continue;
            var handler = createHandler(method);
            handlers.add(handler);
        }
    }

    @Contract("_ -> fail")
    @SneakyThrows
    private static void sneakyThrow(Throwable t) {
        throw t;
    }

    private static boolean isHandler(Method method) {
        return method.isAnnotationPresent(HandleException.class);
    }

    protected static @NotNull String fmt(@NotNull Number number) {
        return NumberFormat.getInstance().format(number);
    }

    private @NotNull CommandExceptionHandler<A> createHandler(Method method) {
        @SuppressWarnings("unchecked")
        HandlerParameterSupplier<A>[] suppliers = new HandlerParameterSupplier[method.getParameterCount()];
        List<HandlerPredicate<A>> conditions = new ArrayList<>();

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();
            if (Throwable.class.isAssignableFrom(type)) {
                /* handle a Throwable parameter */
                conditions.add((throwable, errorContext) -> type.isAssignableFrom(throwable.getClass()));
                suppliers[i] = (throwable, errorContext) -> throwable;
            } else if (ExecutionContext.class.isAssignableFrom(type)) {
                /* handler an ExecutionContext parameter */
                conditions.add((throwable, errorContext) -> errorContext.hasExecutionContext());
                suppliers[i] = (throwable, errorContext) -> errorContext.executionContext();
            } else if (ErrorContext.class.isAssignableFrom(type)) {
                /* handler an ErrorContext parameter */
                conditions.add((throwable, errorContext) -> type.isAssignableFrom(errorContext.getClass()));
                suppliers[i] = (throwable, errorContext) -> errorContext;
            } else if (CommandActor.class.isAssignableFrom(type)) {
                /* handle a CommandActor parameter */
                suppliers[i] = (throwable, errorContext) -> errorContext.actor();
            } else if (Lamp.class.isAssignableFrom(type)) {
                /* handle a Lamp parameter */
                suppliers[i] = (throwable, errorContext) -> errorContext.lamp();
            } else if (ExecutableCommand.class.isAssignableFrom(type)) {
                /* handler an ExecutableCommand parameter */
                conditions.add((throwable, errorContext) -> errorContext.hasExecutionContext());
                suppliers[i] = (throwable, errorContext) -> errorContext.executionContext().command();
            } else if (CommandFunction.class.isAssignableFrom(type)) {
                /* handle a CommandFunction parameter */
                conditions.add((throwable, errorContext) -> errorContext.hasExecutionContext());
                suppliers[i] = (throwable, errorContext) -> errorContext.executionContext().command().function();
            } else if (CommandParameter.class.isAssignableFrom(type)) {
                /* handle a CommandParameter parameter */
                conditions.add((throwable, errorContext) -> errorContext instanceof ParsingParameter<A>);
                suppliers[i] = (throwable, errorContext) -> ((ParsingParameter<A>) errorContext).parameter().parameter();
            } else if (ParameterNode.class.isAssignableFrom(type)) {
                /* handle a ParameterNode parameter */
                conditions.add((throwable, errorContext) -> errorContext instanceof ParsingParameter<A>);
                suppliers[i] = (throwable, errorContext) -> ((ParsingParameter<A>) errorContext).parameter();
            } else if (LiteralNode.class.isAssignableFrom(type)) {
                /* handle a LiteralNode parameter */
                conditions.add((throwable, errorContext) -> errorContext instanceof ParsingLiteral<A>);
                suppliers[i] = (throwable, errorContext) -> ((ParsingLiteral<A>) errorContext).literal();
            } else {
                throw new IllegalArgumentException("Don't know how to handle parameter of type " + type + " for a @HandleException function (" + method + ")");
            }
        }
        return (throwable, errorContext) -> {
            for (HandlerPredicate<A> condition : conditions) {
                if (!condition.test(throwable, errorContext))
                    return;
            }
            Object[] arguments = new Object[parameters.length];
            for (int i = 0; i < suppliers.length; i++) {
                HandlerParameterSupplier<A> supplier = suppliers[i];
                arguments[i] = supplier.supply(throwable, errorContext);
            }
            try {
                method.invoke(this, arguments);
            } catch (IllegalAccessException e) {
                sneakyThrow(e);
            } catch (InvocationTargetException e) {
                sneakyThrow(e.getCause());
            }
        };
    }

    @Override
    public final void handleException(@NotNull Throwable throwable, @NotNull ErrorContext<A> errorContext) {
        for (var handler : handlers) {
            handler.handleException(throwable, errorContext);
        }
    }

    /**
     * Annotation for creating handler functions. The signature of the function determines
     * the input and invocation conditions of it.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface HandleException {
    }

    private interface HandlerParameterSupplier<A extends CommandActor> {

        Object supply(@NotNull Throwable throwable, @NotNull ErrorContext<A> errorContext);

    }

    private interface HandlerPredicate<A extends CommandActor> {

        boolean test(@NotNull Throwable throwable, @NotNull ErrorContext<A> errorContext);

    }

}
