package revxrsal.commands.core;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.*;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.reflect.MethodCaller.BoundMethodCaller;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ParameterValidator;
import revxrsal.commands.process.ResponseHandler;
import revxrsal.commands.util.Preconditions;
import revxrsal.commands.util.Primitives;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.addAll;
import static java.util.stream.Collectors.toMap;
import static revxrsal.commands.core.BaseCommandHandler.ASYNC;
import static revxrsal.commands.core.BaseCommandHandler.DIRECT;
import static revxrsal.commands.util.Collections.listOf;
import static revxrsal.commands.util.Strings.getName;
import static revxrsal.commands.util.Strings.splitBySpace;

final class CommandParser {

    private static final AtomicInteger COMMAND_ID = new AtomicInteger();

    private CommandParser() {}

    public static void parse(@NotNull BaseCommandHandler handler, CommandCompound registration, @NotNull Object boundTarget) {
        Class<?> type = boundTarget instanceof Class ? (Class<?>) boundTarget : boundTarget.getClass();
       parse(handler, registration, type, boundTarget);
    }

    @SneakyThrows
    public static void parse(@NotNull BaseCommandHandler handler, CommandCompound registration, @NotNull Class<?> container, @NotNull Object boundTarget) {
        Map<CommandPath, CommandExecutable> executables = registration.getExecutables();
        Map<CommandPath, BaseCommandCategory> categories = registration.getSubcategories();
        Map<CommandPath, CommandExecutable> subactions = new HashMap<>();
        for (Method method : container.getDeclaredMethods()) {
            AnnotationReader reader = new AnnotationReader(method);
            if (reader.isEmpty()) continue;
            List<CommandPath> paths = getCommandPath(method, reader);
            BoundMethodCaller caller = handler.getMethodCallerFactory().createFor(method).bindTo(boundTarget);
            int id = COMMAND_ID.getAndIncrement();
            paths.forEach(path -> {
                for (BaseCommandCategory category : getCategories(path)) {
                    categories.putIfAbsent(category.path, category);
                }
                LinkedList<String> pathList = path.toList();
                CommandExecutable executable = new CommandExecutable();
                executable.name = pathList.removeLast();
                executable.id = id;
                executable.handler = handler;
                executable.description = reader.get(Description.class, Description::value);
                executable.path = path;
                executable.method = method;
                executable.reader = reader;
                executable.secret = reader.contains(SecretCommand.class);
                executable.methodCaller = caller;
                executable.parent(categories.get(path.getCategoryPath()));
                executable.responseHandler = getResponseHandler(handler, method);
                executable.parameters = getParameters(handler, method, executable);
                executable.resolveableParameters = executable.parameters.stream()
                        .filter(c -> c.getCommandIndex() != -1)
                        .collect(toMap(CommandParameter::getCommandIndex, c -> c));
                executable.executor = handleExecutorExceptions(handler, isAsync(reader, method) ? ASYNC : DIRECT);
                executable.usage = reader.get(Usage.class, Usage::value, () -> generateUsage(executable));
                if (reader.contains(Default.class))
                    subactions.put(path, executable);
                else
                    putOrError(executables, path, executable, "A command with path '" + path.toRealString() + "' already exists!");
            });
        }

        subactions.forEach((path, subaction) -> {
            BaseCommandCategory cat = categories.get(path);
            if (cat != null) { // should never be null but let's just do that
                cat.defaultAction = subaction;
            }
        });
    }

    private static boolean isAsync(AnnotationReader reader, Method method) {
        return reader.contains(RunAsync.class) || CompletionStage.class.isAssignableFrom(method.getReturnType());
    }

    private static String generateUsage(@NotNull ExecutableCommand command) {
        StringJoiner joiner = new StringJoiner(" ");
        CommandHandler handler = command.getCommandHandler();
        for (CommandParameter parameter : command.getValueParameters().values()) {
            if (!parameter.getResolver().mutatesArguments()) continue;
            if (parameter.isSwitch()) {
                joiner.add("[" + handler.getSwitchPrefix() + parameter.getSwitchName() + "]");
            } else if (parameter.isFlag()) {
                joiner.add("[" + handler.getFlagPrefix() + parameter.getFlagName() + " <value>]");
            } else {
                if (parameter.isOptional())
                    joiner.add("[" + parameter.getName() + "]");
                else
                    joiner.add("<" + parameter.getName() + ">");
            }
        }
        return joiner.toString();
    }

    @SuppressWarnings("rawtypes")
    private static ResponseHandler<?> getResponseHandler(BaseCommandHandler handler, Method method) {
        Class<?> returnType = method.getReturnType();
        if (CompletionStage.class.isAssignableFrom(returnType)) {
            Class<?> actualRawType;
            try {
                Type actualType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                actualRawType = Primitives.getRawType(actualType);
                if (CompletionStage.class.isAssignableFrom(actualRawType)) {
                    throw new IllegalStateException("Cannot have a CompletionStage of a CompletionStage!");
                }
            } catch (ClassCastException e) {
                actualRawType = Object.class;
            }
            ResponseHandler delegateHandler = handler.responseHandlers.getFlexibleOrDefault(actualRawType, ResponseHandler.VOID);
            return new CompletionStageResponseHandler(handler, delegateHandler);
        }
        return handler.responseHandlers.getFlexibleOrDefault(method.getReturnType(), ResponseHandler.VOID);
    }

    private static Set<BaseCommandCategory> getCategories(@NotNull CommandPath path) {
        String parent = path.getParent();
        Set<BaseCommandCategory> categories = new HashSet<>();

        BaseCommandCategory root = new BaseCommandCategory();
        root.path = CommandPath.get(parent);
        root.name = parent;
        categories.add(root);

        List<String> pathList = new ArrayList<>();
        pathList.add(parent);

        for (String subcommand : path.getSubcommandPath()) {
            pathList.add(subcommand);
            BaseCommandCategory cat = new BaseCommandCategory();
            cat.path = CommandPath.get(pathList);
            cat.name = cat.path.getName();
            categories.add(cat);
        }

        return categories;
    }

    private static List<CommandParameter> getParameters(@NotNull BaseCommandHandler handler,
                                                        @NotNull Method method,
                                                        @NotNull CommandExecutable parent) {
        List<CommandParameter> parameters = new ArrayList<>();
        Parameter[] methodParameters = method.getParameters();
        int cIndex = 0;
        for (int i = 0; i < methodParameters.length; i++) {
            Parameter parameter = methodParameters[i];
            AnnotationReader paramAnns = new AnnotationReader(parameter);
            List<ParameterValidator<Object>> validators = new ArrayList<>(
                    handler.validators.getFlexibleOrDefault(parameter.getType(), Collections.emptyList())
            );
            BaseCommandParameter param = new BaseCommandParameter(
                    getName(parameter),
                    paramAnns.get(Description.class, Description::value),
                    i,
                    paramAnns.get(Default.class, Default::value),
                    i == methodParameters.length - 1 && !paramAnns.contains(Single.class),
                    paramAnns.contains(Optional.class) || paramAnns.contains(Default.class),
                    parent,
                    parameter,
                    paramAnns.get(Switch.class),
                    paramAnns.get(Flag.class),
                    Collections.unmodifiableList(validators)
            );

            if (param.getType().isPrimitive() && param.isOptional() && param.getDefaultValue() == null && !param.isSwitch())
                throw new IllegalStateException("Optional parameter " + parameter + " at " + method + " cannot be a prmitive!");
            if (param.isSwitch()) {
                if (Primitives.wrap(param.getType()) != Boolean.class)
                    throw new IllegalStateException("Switch parameter " + parameter + " at " + method + " must be of boolean type!");
            }

            ParameterResolver<?> resolver = handler.getResolver(param);

            if (resolver == null)
                throw new IllegalStateException("Unable to find a resolver for parameter type " + parameter.getType());

            param.resolver = resolver;
            if (resolver.mutatesArguments())
                param.cindex = cIndex++;
            param.suggestionProvider = handler.autoCompleter.resolveSuggestionProvider(param);
            parameters.add(param);
        }
        return Collections.unmodifiableList(parameters);
    }

    private static List<CommandPath> getCommandPath(@NotNull Method method, @NotNull AnnotationReader reader) {
        List<CommandPath> paths = new ArrayList<>();

        List<String> commands = new ArrayList<>();
        List<String> subcommands = new ArrayList<>();
        Command commandAnnotation = reader.get(Command.class, "Method " + method.getName() + " does not have a parent command!");
        Preconditions.notEmpty(commandAnnotation.value(), "@Command#value() cannot be an empty array!");
        addAll(commands, commandAnnotation.value());

        List<String> parentSubcommandAliases = new ArrayList<>();

        for (Class<?> topClass : getTopClasses(method.getDeclaringClass())) {
            Subcommand ps = topClass.getAnnotation(Subcommand.class);
            if (ps != null) {
                addAll(parentSubcommandAliases, ps.value());
            }
        }

        Subcommand subcommandAnnotation = reader.get(Subcommand.class);
        if (subcommandAnnotation != null) {
            addAll(subcommands, subcommandAnnotation.value());
        }

        for (String command : commands) {
            if (!subcommands.isEmpty()) {
                for (String subcommand : subcommands) {
                    List<String> path = new ArrayList<>(splitBySpace(command));
                    parentSubcommandAliases.forEach(subcommandAlias -> path.addAll(splitBySpace(subcommandAlias)));
                    path.addAll(splitBySpace(subcommand));
                    paths.add(CommandPath.get(path));
                }
            } else {
                paths.add(CommandPath.get(splitBySpace(command)));
            }
        }
        return paths;
    }

    private static List<Class<?>> getTopClasses(Class<?> c) {
        List<Class<?>> classes = listOf(c);
        Class<?> enclosingClass = c.getEnclosingClass();
        while (c.getEnclosingClass() != null) {
            classes.add(c = enclosingClass);
        }
        Collections.reverse(classes);
        return classes;
    }

    private static @NotNull Executor handleExecutorExceptions(CommandHandler handler, Executor executor) {
        return task -> executor.execute(handleRunnableExceptions(handler, task));
    }

    private static @NotNull Runnable handleRunnableExceptions(CommandHandler handler, Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                handler.getExceptionHandler().handleException(t);
            }
        };
    }

    private static <K, V> void putOrError(Map<K, V> map, K key, V value, String err) {
        if (map.containsKey(key)) {
            throw new IllegalStateException(err);
        }
        map.put(key, value);
    }

}
