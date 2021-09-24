package revxrsal.commands.core;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.*;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ParameterValidator;

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
        handler.conditions.forEach(condition -> condition.test(actor, executable, args.asImmutableView()));
        Object[] methodArguments = getMethodArguments(executable, actor, args);
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
    private Object[] getMethodArguments(CommandExecutable executable, CommandActor actor, ArgumentStack args) {
        Object[] values = new Object[executable.parameters.size()];
        for (CommandParameter parameter : executable.parameters) {
            if (ArgumentStack.class.isAssignableFrom(parameter.getType()))
                values[parameter.getMethodIndex()] = args;
            else if (parameter.isSwitch())
                handleSwitch(args, values, parameter);
            else if (parameter.isFlag())
                handleFlag(actor, args, values, parameter);
        }
        for (CommandParameter parameter : executable.parameters) {
            if (!parameter.isSwitch() && !parameter.isFlag() && !ArgumentStack.class.isAssignableFrom(parameter.getType())) {
                ParameterResolver<?> resolver = parameter.getResolver();
                if (!resolver.mutatesArguments()) {
                    Object value = resolver.resolve(actor, parameter, executable, args);
                    for (ParameterValidator<Object> v : parameter.getValidators()) {
                        v.validate(value, parameter, actor);
                    }
                    values[parameter.getMethodIndex()] = value;
                } else {
                    if (!addDefaultValues(args, parameter, actor, values)) {
                        Object value = resolver.resolve(actor, parameter, executable, args);
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

    @SneakyThrows private void handleFlag(CommandActor actor, ArgumentStack args, Object[] values, CommandParameter parameter) {
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
        Object value = parameter.getResolver().resolve(actor, parameter, parameter.getDeclaringCommand(), flagArguments);
        for (ParameterValidator<Object> v : parameter.getValidators()) {
            v.validate(value, parameter, actor);
        }
        values[parameter.getMethodIndex()] = value;

    }

}
