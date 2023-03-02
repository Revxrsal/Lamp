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
package revxrsal.commands.core;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.*;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ParameterResolver.ParameterResolverContext;
import revxrsal.commands.process.ParameterValidator;
import revxrsal.commands.process.ValueResolver.ValueResolverContext;

import java.util.List;
import java.util.function.Function;

public final class BaseCommandDispatcher {

    private final BaseCommandHandler handler;

    public BaseCommandDispatcher(BaseCommandHandler handler) {
        this.handler = handler;
    }

    public Object eval(@NotNull CommandActor actor, @NotNull ArgumentStack arguments) {
        try {
            MutableCommandPath path = MutableCommandPath.empty();
            String argument = arguments.getFirst();
            path.add(argument);
            CommandExecutable executable = handler.executables.get(path);
            if (executable != null) {
                arguments.removeFirst();
                return execute(executable, actor, arguments);
            }

            BaseCommandCategory category = handler.categories.get(path);
            if (category != null) {
                arguments.removeFirst();
                return searchCategory(actor, category, path, arguments);
            } else {
                throw new InvalidCommandException(path, path.getFirst());
            }
        } catch (Throwable throwable) {
            handler.getExceptionHandler().handleException(throwable, actor);
        }
        return null;
    }

    private Object searchCategory(CommandActor actor, BaseCommandCategory category, MutableCommandPath path, ArgumentStack arguments) {
        if (!arguments.isEmpty()) {
            path.add(arguments.getFirst());
        }
        CommandExecutable executable = (CommandExecutable) category.commands.get(path);
        if (executable != null) {
            arguments.removeFirst();
            return execute(executable, actor, arguments);
        }
        category.checkPermission(actor);
        BaseCommandCategory found = (BaseCommandCategory) category.getCategories().get(path);
        if (found == null) {
            if (category.defaultAction == null)
                throw new NoSubcommandSpecifiedException(category);
            else {
                return execute(category.defaultAction, actor, arguments);
            }
        } else {
            arguments.removeFirst();
            return searchCategory(actor, found, path, arguments);
        }
    }

    private Object execute(@NotNull CommandExecutable executable,
                           @NotNull CommandActor actor,
                           @NotNull ArgumentStack args) {
        List<String> input = args.asImmutableCopy();
        handler.conditions.forEach(condition -> condition.test(actor, executable, args.asImmutableView()));
        Object[] methodArguments = getMethodArguments(executable, actor, args, input);
        if (!args.isEmpty() && handler.failOnExtra) {
            throw new TooManyArgumentsException(executable, args);
        }
        Object result;
        try {
            result = executable.methodCaller.call(methodArguments);
        } catch (Throwable throwable) {
            throw new CommandInvocationException(executable, throwable);
        }
        executable.responseHandler.handleResponse(result, actor, executable);
        return result;
    }

    @SneakyThrows
    private Object[] getMethodArguments(CommandExecutable executable, CommandActor actor, ArgumentStack args, List<String> input) {
        Object[] values = new Object[executable.parameters.size()];
        for (CommandParameter parameter : executable.parameters) {
            if (ArgumentStack.class.isAssignableFrom(parameter.getType()))
                values[parameter.getMethodIndex()] = args;
            else if (parameter.isSwitch())
                handleSwitch(args, values, parameter);
            else if (parameter.isFlag())
                handleFlag(input, actor, args, values, parameter);
        }
        for (CommandParameter parameter : executable.parameters) {
            if (ArgumentStack.class.isAssignableFrom(parameter.getType())) {
                values[parameter.getMethodIndex()] = args;
                continue;
            }
            if (!parameter.isSwitch() && !parameter.isFlag()) {
                ParameterResolver<?> resolver = parameter.getResolver();
                if (!resolver.mutatesArguments()) {
                    parameter.checkPermission(actor);
                    ContextResolverContext cxt = new ContextResolverContext(input, actor, parameter, values);
                    Object value = resolver.resolve(cxt);
                    for (ParameterValidator<Object> v : parameter.getValidators()) {
                        v.validate(value, parameter, actor);
                    }
                    values[parameter.getMethodIndex()] = value;
                } else {
                    if (!addDefaultValues(args, parameter, values)) {
                        parameter.checkPermission(actor);
                        ValueContextR cxt = new ValueContextR(input, actor, parameter, values, args);
                        Object value = resolver.resolve(cxt);
                        for (ParameterValidator<Object> v : parameter.getValidators()) {
                            v.validate(value, parameter, actor);
                        }
                        values[parameter.getMethodIndex()] = value;
                    }
                }
            }
        }
        return values;
    }

    private boolean addDefaultValues(ArgumentStack args,
                                     CommandParameter parameter,
                                     Object[] values) {
        if (args.isEmpty()) {
            if (parameter.getDefaultValue().contains("<?null>")) {
                values[parameter.getMethodIndex()] = null;
                return true;
            } else {
                if (!parameter.getDefaultValue().isEmpty()) {
                    args.addAll(parameter.getDefaultValue());
                    return false;
                } else {
                    throw new MissingArgumentException(parameter);
                }
            }
        }
        return false;
    }

    private void handleSwitch(ArgumentStack args, Object[] values, CommandParameter parameter) {
        boolean provided = args.remove(handler.switchPrefix + parameter.getSwitchName());
        if (!provided)
            values[parameter.getMethodIndex()] = parameter.getDefaultSwitch();
        else
            values[parameter.getMethodIndex()] = true;
    }

    @SneakyThrows private void handleFlag(List<String> input, CommandActor actor, ArgumentStack args, Object[] values, CommandParameter parameter) {
        String lookup = handler.getFlagPrefix() + parameter.getFlagName();
        int index = args.indexOf(lookup);
        ArgumentStack flagArguments;
        if (index == -1) { // flag isn't specified, use default value or throw an MPE.
            if (parameter.isOptional()) {
                if (!parameter.getDefaultValue().isEmpty()) {
                    args.add(lookup);
                    args.addAll(parameter.getDefaultValue());
                    index = args.indexOf(lookup);
                    args.remove(index); // remove the flag prefix + flag name
                    flagArguments = ArgumentStack.parse(args.remove(index)); // put the actual value in a separate argument stack
                } else {
                    for (ParameterValidator<Object> v : parameter.getValidators()) {
                        v.validate(null, parameter, actor);
                    }
                    values[parameter.getMethodIndex()] = null;
                    return;
                }
            } else {
                throw new MissingArgumentException(parameter);
            }
        } else {
            args.remove(index); // remove the flag prefix + flag name
            if (index >= args.size())
                throw new MissingArgumentException(parameter);
            flagArguments = ArgumentStack.parse(args.remove(index)); // put the actual value in a separate argument stack
        }
        ValueContextR contextR = new ValueContextR(input, actor, parameter, values, flagArguments);
        Object value = parameter.getResolver().resolve(contextR);
        for (ParameterValidator<Object> v : parameter.getValidators()) {
            v.validate(value, parameter, actor);
        }
        values[parameter.getMethodIndex()] = value;

    }

    @AllArgsConstructor
    private static abstract class ParamResolverContext implements ParameterResolverContext {

        private final List<String> input;
        private final CommandActor actor;
        private final CommandParameter parameter;
        private final Object[] resolved;

        @Override public @NotNull @Unmodifiable List<String> input() {
            return input;
        }

        @Override public <A extends CommandActor> @NotNull A actor() {
            return (A) actor;
        }

        @Override public @NotNull CommandParameter parameter() {
            return parameter;
        }

        @Override public @NotNull ExecutableCommand command() {
            return parameter.getDeclaringCommand();
        }

        @Override public @NotNull CommandHandler commandHandler() {
            return parameter.getCommandHandler();
        }

        @Override public <T> @NotNull T getResolvedParameter(@NotNull CommandParameter parameter) {
            try {
                return (T) resolved[parameter.getMethodIndex()];
            } catch (Throwable throwable) {
                throw new IllegalArgumentException("This parameter has not been resolved yet!");
            }
        }

        @Override public <T> @NotNull T getResolvedArgument(@NotNull Class<T> type) {
            for (Object o : resolved) {
                if (type.isInstance(o))
                    return (T) o;
            }
            throw new IllegalArgumentException("This parameter has not been resolved yet!");
        }
    }

    private static final class ContextResolverContext extends ParamResolverContext implements ContextResolver.ContextResolverContext {

        public ContextResolverContext(List<String> input, CommandActor actor, CommandParameter parameter, Object[] resolved) {
            super(input, actor, parameter, resolved);
        }
    }

    static final class ValueContextR extends ParamResolverContext implements ValueResolverContext {

        ArgumentStack argumentStack;

        public ValueContextR(List<String> input,
                             CommandActor actor,
                             CommandParameter parameter,
                             Object[] resolved,
                             ArgumentStack argumentStack) {
            super(input, actor, parameter, resolved);
            this.argumentStack = argumentStack;
        }

        @Override public ArgumentStack arguments() {
            return argumentStack;
        }

        @Override public String popForParameter() {
            return arguments().popForParameter(parameter());
        }

        @Override public String pop() {
            return arguments().pop();
        }

        private <T> T num(Function<String, T> f) {
            String input = pop();
            try {
                if (input.startsWith("0x"))
                    return (T) Integer.valueOf(input.substring(2), 16);
                return f.apply(input);
            } catch (NumberFormatException e) {
                throw new InvalidNumberException(parameter(), input);
            }
        }

        @Override public int popInt() {
            return num(Integer::parseInt);
        }

        @Override public double popDouble() {
            return num(Double::parseDouble);
        }

        @Override public byte popByte() {
            return num(Byte::parseByte);
        }

        @Override public short popShort() {
            return num(Short::parseShort);
        }

        @Override public float popFloat() {
            return num(Float::parseFloat);
        }

        @Override public long popLong() {
            return num(Long::parseLong);
        }
    }
}
