package revxrsal.commands.core;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.*;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.reflect.MethodCaller.BoundMethodCaller;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.OrphanRegistry;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ParameterValidator;
import revxrsal.commands.process.PermissionReader;
import revxrsal.commands.process.ResponseHandler;
import revxrsal.commands.util.Preconditions;
import revxrsal.commands.util.Primitives;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.util.Collections.addAll;
import static java.util.stream.Collectors.toMap;
import static revxrsal.commands.util.Collections.listOf;
import static revxrsal.commands.util.Strings.getName;
import static revxrsal.commands.util.Strings.splitBySpace;

final class CommandParser {

    static final ResponseHandler<?> VOID_HANDLER = (response, actor, command) -> {};
    private static final AtomicInteger COMMAND_ID = new AtomicInteger();

    private CommandParser() {}

    public static void parse(@NotNull BaseCommandHandler handler, @NotNull OrphanRegistry orphan) {
        OrphanCommand instance = orphan.getHandler();
        Class<?> type = instance.getClass();

        // we pass the type of the orphan handler, but pass the object as the orphan registry
        parse(handler, type, orphan);
    }

    public static void parse(@NotNull BaseCommandHandler handler, @NotNull Object boundTarget) {
        Class<?> type = boundTarget instanceof Class ? (Class<?>) boundTarget : boundTarget.getClass();
        parse(handler, type, boundTarget);
    }

    @SneakyThrows
    public static void parse(@NotNull BaseCommandHandler handler, @NotNull Class<?> container, @NotNull Object boundTarget) {
        Map<CommandPath, BaseCommandCategory> categories = handler.categories;
        Map<CommandPath, CommandExecutable> subactions = new HashMap<>();
        for (Method method : getAllMethods(container)) {
            AnnotationReader reader = AnnotationReader.create(handler, method);
            Object invokeTarget = boundTarget;
            if (reader.shouldDismiss()) continue;
            if (boundTarget instanceof OrphanRegistry) {
                insertCommandPath((OrphanRegistry) boundTarget, reader);
                invokeTarget = ((OrphanRegistry) invokeTarget).getHandler();
            }
            reader.distributeAnnotations();
            reader.replaceAnnotations(handler);
            List<CommandPath> paths = getCommandPath(container, method, reader);
            BoundMethodCaller caller = handler.getMethodCallerFactory().createFor(method).bindTo(invokeTarget);
            int id = COMMAND_ID.getAndIncrement();
            boolean isDefault = reader.contains(Default.class);
            paths.forEach(path -> {
                for (BaseCommandCategory category : getCategories(handler, isDefault, path)) {
                    categories.putIfAbsent(category.path, category);
                }
                CommandExecutable executable = new CommandExecutable();
                if (!isDefault) categories.remove(path); // prevent duplication.
                executable.name = path.getLast();
                executable.id = id;
                executable.handler = handler;
                executable.description = reader.get(Description.class, Description::value);
                executable.path = path;
                executable.method = method;
                executable.reader = reader;
                executable.secret = reader.contains(SecretCommand.class);
                executable.methodCaller = caller;
                if (isDefault)
                    executable.parent(categories.get(path));
                else
                    executable.parent(categories.get(path.getCategoryPath()));
                executable.responseHandler = getResponseHandler(handler, method.getGenericReturnType());
                executable.parameters = getParameters(handler, method, executable);
                executable.resolveableParameters = executable.parameters.stream()
                        .filter(c -> c.getCommandIndex() != -1)
                        .collect(toMap(CommandParameter::getCommandIndex, c -> c));
                executable.usage = reader.get(Usage.class, Usage::value, () -> generateUsage(executable));
                if (reader.contains(Default.class))
                    subactions.put(path, executable);
                else
                    putOrError(handler.executables, path, executable, "A command with path '" + path.toRealString() + "' already exists!");
            });
        }

        subactions.forEach((path, subaction) -> {
            BaseCommandCategory cat = categories.get(path);
            if (cat != null) { // should never be null but let's just do that
                cat.defaultAction = subaction;
            }
        });
    }

    private static void insertCommandPath(OrphanRegistry boundTarget, AnnotationReader reader) {
        List<CommandPath> paths = boundTarget.getParentPaths();
        String[] pathsArray = paths.stream().map(CommandPath::toRealString).toArray(String[]::new);
        reader.add(new Command() {
            @Override public Class<? extends Annotation> annotationType() {return Command.class;}

            @Override public String[] value() {return pathsArray;}
        });
    }

    private static Set<Method> getAllMethods(Class<?> c) {
        Set<Method> methods = new HashSet<>();
        Class<?> current = c;
        while (current != null && current != Object.class) {
            addAll(methods, current.getDeclaredMethods());
            current = current.getSuperclass();
        }
        return methods;
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
    private static ResponseHandler<?> getResponseHandler(BaseCommandHandler handler, Type genericType) {
        Class<?> rawType = Primitives.getRawType(genericType);
        if (CompletionStage.class.isAssignableFrom(rawType)) {
            ResponseHandler delegateHandler = getResponseHandler(handler, getInsideGeneric(genericType));
            return new CompletionStageResponseHandler(handler, delegateHandler);
        }
        if (java.util.Optional.class.isAssignableFrom(rawType)) {
            ResponseHandler delegateHandler = getResponseHandler(handler, getInsideGeneric(genericType));
            return new OptionalResponseHandler(delegateHandler);
        }
        if (Supplier.class.isAssignableFrom(rawType)) {
            ResponseHandler delegateHandler = getResponseHandler(handler, getInsideGeneric(genericType));
            return new SupplierResponseHandler(delegateHandler);
        }
        return handler.responseHandlers.getFlexibleOrDefault(rawType, VOID_HANDLER);
    }

    private static Type getInsideGeneric(Type genericType) {
        try {
            return ((ParameterizedType) genericType).getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            return Object.class;
        }
    }

    private static Set<BaseCommandCategory> getCategories(CommandHandler handler, boolean respectDefault, @NotNull CommandPath path) {
        if (path.size() == 1 && !respectDefault) return Collections.emptySet();
        String parent = path.getParent();
        Set<BaseCommandCategory> categories = new HashSet<>();

        BaseCommandCategory root = new BaseCommandCategory();
        root.handler = handler;
        root.path = CommandPath.get(parent);
        root.name = parent;
        categories.add(root);

        List<String> pathList = new ArrayList<>();
        pathList.add(parent);

        for (String subcommand : path.getSubcommandPath()) {
            pathList.add(subcommand);
            BaseCommandCategory cat = new BaseCommandCategory();
            cat.handler = handler;
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
            AnnotationReader paramAnns = AnnotationReader.create(handler, parameter);
            List<ParameterValidator<Object>> validators = new ArrayList<>(
                    handler.validators.getFlexibleOrDefault(parameter.getType(), Collections.emptyList())
            );
            String[] defaultValue = paramAnns.get(Default.class, Default::value);
            BaseCommandParameter param = new BaseCommandParameter(
                    getName(parameter),
                    paramAnns.get(Description.class, Description::value),
                    i,
                    defaultValue == null ? Collections.emptyList() : Collections.unmodifiableList(Arrays.asList(defaultValue)),
                    i == methodParameters.length - 1 && !paramAnns.contains(Single.class),
                    paramAnns.contains(Optional.class) || paramAnns.contains(Default.class),
                    parent,
                    parameter,
                    paramAnns.get(Switch.class),
                    paramAnns.get(Flag.class),
                    Collections.unmodifiableList(validators)
            );

            for (PermissionReader reader : handler.getPermissionReaders()) {
                CommandPermission permission = reader.getPermission(param);
                if (permission != null) {
                    param.permission = permission;
                    break;
                }
            }

            if (param.getType().isPrimitive() && param.isOptional() && param.getDefaultValue().isEmpty() && !param.isSwitch())
                throw new IllegalStateException("Optional parameter " + parameter + " at " + method + " cannot be a prmitive!");
            if (param.isSwitch()) {
                if (Primitives.wrap(param.getType()) != Boolean.class)
                    throw new IllegalStateException("Switch parameter " + parameter + " at " + method + " must be of boolean type!");
            }
            ParameterResolver<?> resolver;
            if (param.getType() == ArgumentStack.class) {
                resolver = new Resolver(context -> ArgumentStack.copy(context.input()), null);
            } else
                resolver = handler.getResolver(param);

            if (resolver == null) {
                throw new IllegalStateException("Unable to find a resolver for parameter type " + parameter.getType());
            }
            param.resolver = resolver;
            if (resolver.mutatesArguments())
                param.cindex = cIndex++;
            param.suggestionProvider = handler.autoCompleter.getProvider(param);
            parameters.add(param);
        }
        return Collections.unmodifiableList(parameters);
    }

    private static List<CommandPath> getCommandPath(@NotNull Class<?> container,
                                                    @NotNull Method method,
                                                    @NotNull AnnotationReader reader) {
        List<CommandPath> paths = new ArrayList<>();

        List<String> commands = new ArrayList<>();
        List<String> subcommands = new ArrayList<>();
        Command commandAnnotation = reader.get(Command.class, "Method " + method.getName() + " does not have a parent command! You might have forgotten one of the following:\n" +
                "- @Command on the method or class\n" +
                "- implement OrphanCommand");
        Preconditions.notEmpty(commandAnnotation.value(), "@Command#value() cannot be an empty array!");
        addAll(commands, commandAnnotation.value());

        List<String> parentSubcommandAliases = new ArrayList<>();

        for (Class<?> topClass : getTopClasses(container)) {
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

    private static <K, V> void putOrError(Map<K, V> map, K key, V value, String err) {
        if (map.containsKey(key)) {
            throw new IllegalStateException(err);
        }
        map.put(key, value);
    }

}
