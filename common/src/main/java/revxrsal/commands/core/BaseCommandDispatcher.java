package revxrsal.commands.core;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
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
import java.util.Optional;

public final class BaseCommandDispatcher {

    private final BaseCommandHandler handler;

    public BaseCommandDispatcher(BaseCommandHandler handler) {
        this.handler = handler;
    }

    public Object eval(@NotNull CommandActor actor, @NotNull ArgumentStack arguments) {
        try {
            MutableCommandPath path = MutableCommandPath.empty();
            BaseCommandCategory lastCategory = null;
            String lastArgument = null;
            for (int i = 0; i < arguments.size(); i++) {
                String argument = lastArgument = arguments.get(i);
                path.add(argument);

                CommandExecutable executable = (CommandExecutable) handler.getCommand(path);
                if (executable != null) {
                    arguments.subList(0, i + 1).clear();
                    return execute(executable, actor, arguments);
                }

                BaseCommandCategory cat = (BaseCommandCategory) handler.getCategory(path);
                if (cat != null) {
                    lastCategory = cat;
                    CommandExecutable defaultAction = cat.defaultAction;
                    if (defaultAction != null && i + 1 == arguments.size()) {
                        arguments.subList(0, i).clear();
                        return execute(defaultAction, actor, arguments);
                    } else if (i == arguments.size() - 1 && defaultAction == null) {
                        throw new NoSubcommandSpecifiedException(cat);
                    }
                }
                if (i == arguments.size() - 1 && lastCategory != null && lastCategory.defaultAction != null) {
                    arguments.subList(0, i).clear();
                    return execute(lastCategory.defaultAction, actor, arguments);
                }
            }
            if (lastCategory != null) {
                if (lastCategory.defaultAction != null) {
                    arguments.removeFirst();
                    return execute(lastCategory.defaultAction, actor, arguments);
                } else
                    throw new InvalidSubcommandException(path, path.getLast());
            } else {
                if (lastArgument != null)
                    throw new InvalidCommandException(path, lastArgument);
            }
        } catch (Throwable throwable) {
            if (throwable instanceof SendableException)
                ((SendableException) throwable).sendTo(actor);
            else
                handler.getExceptionHandler().handleException(throwable, actor);
        }
        return null;
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
            if (!parameter.isSwitch() && !parameter.isFlag() && !ArgumentStack.class.isAssignableFrom(parameter.getType())) {
                ParameterResolver<?> resolver = parameter.getResolver();
                if (!resolver.mutatesArguments()) {
                    ContextResolverContext cxt = new ContextResolverContext(input, actor, parameter, values);
                    Object value = resolver.resolve(cxt);
                    for (ParameterValidator<Object> v : parameter.getValidators()) {
                        v.validate(value, parameter, actor);
                    }
                    values[parameter.getMethodIndex()] = value;
                } else {
                    if (!addDefaultValues(args, parameter, actor, values)) {
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
                                     CommandActor actor,
                                     Object[] values) {
        if (args.isEmpty()) {
            if (parameter.getDefaultValue() == null && parameter.isOptional()) {
                values[parameter.getMethodIndex()] = null;
                return true;
            } else {
                if (parameter.getDefaultValue() != null) {
                    args.add(parameter.getDefaultValue());
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
                if (parameter.getDefaultValue() != null) {
                    args.add(lookup);
                    args.add(parameter.getDefaultValue());
                    index = args.indexOf(lookup);
                    args.remove(index); // remove the flag prefix + flag name
                    flagArguments = ArgumentStack.of(args.remove(index)); // put the actual value in a separate argument stack
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
            flagArguments = ArgumentStack.of(args.remove(index)); // put the actual value in a separate argument stack
        }
        ValueContextR contextR = new ValueContextR(input, actor, parameter, values, flagArguments);
        Object value = parameter.getResolver().resolve(contextR);
        for (ParameterValidator<Object> v : parameter.getValidators()) {
            v.validate(value, parameter, actor);
        }
        values[parameter.getMethodIndex()] = value;

    }

    private ParameterResolverContext createContext(List<String> input, CommandActor actor, CommandParameter parameter, Object[] resolved, @NotNull ArgumentStack args) {
        if (parameter.getResolver().mutatesArguments()) // is value
            return new ValueContextR(input, actor, parameter, resolved, args);
        return new ContextResolverContext(input, actor, parameter, resolved);
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

        @Override public <T> Optional<T> getLastArgument(@NotNull Class<T> type) {
            for (Object o : resolved) {
                if (type.isInstance(o))
                    return (Optional<T>) Optional.of(o);
            }
            return Optional.empty();
        }
    }

    private static final class ContextResolverContext extends ParamResolverContext implements ContextResolver.ContextResolverContext {

        public ContextResolverContext(List<String> input, CommandActor actor, CommandParameter parameter, Object[] resolved) {
            super(input, actor, parameter, resolved);
        }
    }

    private static final class ValueContextR extends ParamResolverContext implements ValueResolverContext {

        private final ArgumentStack argumentStack;

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
            return argumentStack.popForParameter(parameter());
        }

        @Override public String pop() {
            return argumentStack.pop();
        }
    }
}
