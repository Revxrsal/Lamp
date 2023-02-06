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
import revxrsal.commands.orphan.Orphans;
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
import java.util.stream.Collectors;

import static java.util.Collections.addAll;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static revxrsal.commands.util.Collections.listOf;
import static revxrsal.commands.util.Strings.getName;
import static revxrsal.commands.util.Strings.splitBySpace;

/**
 * Handles the parsing logic for commands
 */
final class CommandParser {

    /**
     * Handles the response returned by (void) methods
     */
    static final ResponseHandler<?> VOID_HANDLER = (response, actor, command) -> {
    };

    /**
     * A counter for commands IDs
     */
    private static final AtomicInteger COMMAND_ID = new AtomicInteger();

    private CommandParser() {
    }

    /**
     * Parses classes that implement {@link OrphanCommand}. This will
     *
     * @param handler The command handler
     * @param orphan  The orphan constructed from {@link Orphans}
     */
    public static void parse(@NotNull BaseCommandHandler handler, @NotNull OrphanRegistry orphan) {
        OrphanCommand instance = orphan.getHandler();
        Class<?> type = instance.getClass();

        // we pass the type of the orphan handler, but pass the object as the orphan registry
        parse(handler, type, orphan);
    }

    /**
     * Parses the commands in a bound target. A bound target accepts a class (i.e. {@code MyClass.class}),
     * and accepts an instance (i.e. {@code new MyClass()});
     *
     * @param handler     The command handler
     * @param boundTarget The instance to rgeister for
     */
    public static void parse(@NotNull BaseCommandHandler handler, @NotNull Object boundTarget) {
        Class<?> type = boundTarget instanceof Class ? (Class<?>) boundTarget : boundTarget.getClass();
        parse(handler, type, boundTarget);
    }

    @SneakyThrows
    public static void parse(@NotNull BaseCommandHandler handler, @NotNull Class<?> container, @NotNull Object boundTarget) {
        Map<CommandPath, BaseCommandCategory> categories = handler.categories;
        for (Method method : getAllMethods(container)) {
            /* Parse annotations on a method */
            AnnotationReader reader = AnnotationReader.create(handler, method);

            /* How we should invoke methods. This varies between normal commands and orphan commands */
            Object invokeTarget = boundTarget;

            /* Not a command method (i.e. does not contain any annotation that indicates a command) */
            if (reader.shouldDismiss()) continue;

            /* We synthesize a @Command(...) for methods in orphan commands classes */
            if (boundTarget instanceof OrphanRegistry) {
                insertCommandPath((OrphanRegistry) boundTarget, reader);
                invokeTarget = ((OrphanRegistry) invokeTarget).getHandler();
            }

            /* Distribute and replace annotations */
            reader.distributeAnnotations();
            reader.replaceAnnotations(handler);

            /* Generates the command path for the given method. This will take into account
             * the parent class annotations */
            List<CommandPath> paths = getCommandPath(container, method, reader);
            BoundMethodCaller caller = handler.getMethodCallerFactory().createFor(method).bindTo(invokeTarget);

            /* Generate command ID */
            int id = COMMAND_ID.getAndIncrement();

            /* Check if the command is default, and if so, generate a path for it */
            String[] defPaths = reader.get(DefaultFor.class, DefaultFor::value);
            List<CommandPath> defaultPaths = defPaths == null ? emptyList() : Arrays.stream(defPaths)
                    .map(CommandPath::parse)
                    .collect(Collectors.toList());
            boolean isDefault = reader.contains(Default.class) || !defaultPaths.isEmpty();

            /* Generate categories for default paths if not created already */
            for (CommandPath defaultPath : defaultPaths) {
                for (BaseCommandCategory category : generateCategoriesForPath(handler, true, defaultPath)) {
                    categories.putIfAbsent(category.path, category);
                }
            }

            paths.forEach(path -> {

                /* Create categories beforehand, so we can insert commands into them with no problems */
                for (BaseCommandCategory category : generateCategoriesForPath(handler, isDefault, path)) {
                    categories.putIfAbsent(category.path, category);
                }

                Set<CommandPath> defaultPathsAndNormalPath = new HashSet<>();
                defaultPathsAndNormalPath.add(path);
                defaultPathsAndNormalPath.addAll(defaultPaths);
                for (CommandPath p : defaultPathsAndNormalPath) {
                    boolean registerAsDefault = defaultPaths.contains(p);
                    CommandExecutable executable = new CommandExecutable();
                    if (!registerAsDefault)
                        categories.remove(p); // prevent having a category and command with the same path
                    executable.name = p.getLast();
                    executable.id = id;
                    executable.handler = handler;
                    executable.description = reader.get(Description.class, Description::value);
                    executable.path = p;
                    executable.method = method;
                    executable.reader = reader;
                    executable.secret = reader.contains(SecretCommand.class);
                    executable.methodCaller = caller;
                    if (registerAsDefault)
                        executable.parent(categories.get(p), true);
                    else
                        executable.parent(categories.get(p.getCategoryPath()), false);
                    executable.responseHandler = getResponseHandler(handler, method.getGenericReturnType());
                    executable.parameters = getParameters(handler, method, executable);
                    executable.resolveableParameters = executable.parameters.stream()
                            .filter(c -> c.getCommandIndex() != -1)
                            .collect(toMap(CommandParameter::getCommandIndex, c -> c));
                    executable.usage = reader.get(Usage.class, Usage::value, () -> generateUsage(executable));
                    if (!registerAsDefault) {
                        putOrError(handler.executables, p, executable, "A command with path '" + p.toRealString() + "' already exists!");
                    }
                }
            });
        }
    }

    /**
     * Synthesizes a {@link Command} annotation for orphan commands that contains information
     * generated at runtime
     *
     * @param boundTarget Target to inject for
     * @param reader      Reader to add
     */
    private static void insertCommandPath(OrphanRegistry boundTarget, AnnotationReader reader) {
        List<CommandPath> paths = boundTarget.getParentPaths();
        String[] pathsArray = paths.stream().map(CommandPath::toRealString).toArray(String[]::new);
        reader.add(new Command() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Command.class;
            }

            @Override
            public String[] value() {
                return pathsArray;
            }
        });
    }

    /**
     * Finds all {@link Method}s defined by a class, including private ones
     * and ones that are inherited from classes.
     *
     * @param c Class to get for
     * @return A list of all methods
     */
    private static Set<Method> getAllMethods(Class<?> c) {
        Set<Method> methods = new HashSet<>();
        Class<?> current = c;
        while (current != null && current != Object.class) {
            addAll(methods, current.getDeclaredMethods());
            current = current.getSuperclass();
        }
        return methods;
    }

    /**
     * Generates usage syntax for the command. This will wrap optional parameters
     * with squared brackets, and required parameters with angled brackets, as well
     * as flags and switches
     *
     * @param command Command to generate for
     * @return The usage
     */
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

    /**
     * Recursively finds the best response handler for the given command. This will
     * respect {@link CompletionStage}s, {@link Supplier}s, and {@link Optional}s,
     * as well as the generics they have.
     *
     * @param handler     Command handler to find response handlers from
     * @param genericType The return type of the method to find a response handler for
     * @return The response handler
     */
    @SuppressWarnings("rawtypes")
    private static @NotNull ResponseHandler<?> getResponseHandler(BaseCommandHandler handler, Type genericType) {
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

    /**
     * Returns the 1st type inside the generic. This assumer that the type only defines 1
     * generic type (such as {@link Optional} or {@link Supplier})
     *
     * @param genericType The generic type to find from, for example {@code Optional<String>}
     * @return The type inside, for example {@code String}.
     */
    private static Type getInsideGeneric(Type genericType) {
        try {
            return ((ParameterizedType) genericType).getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            return Object.class;
        }
    }

    /**
     * Generates all categories for a path. This will walk through all
     * strings inside the path and create categories as needed.
     *
     * @param handler   Command handler to assign to categories
     * @param isDefault Whether is the given path a default command
     * @param path      The path to generate for
     * @return A set of all categories from the path
     */
    private static Set<BaseCommandCategory> generateCategoriesForPath(
            CommandHandler handler,
            boolean isDefault,
            @NotNull CommandPath path
    ) {
        // must be an actual command, so don't generate any categories
        if (path.size() == 1 && !isDefault)
            return Collections.emptySet();
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

    /**
     * Generates parameters for the given command, and checks their validity
     *
     * @param handler Handler to assign to parameters
     * @param method  Method to parse for
     * @param command The command to parse parameters for
     * @return A list of all command parameters
     */
    private static List<CommandParameter> getParameters(@NotNull BaseCommandHandler handler,
                                                        @NotNull Method method,
                                                        @NotNull CommandExecutable command) {
        List<CommandParameter> parameters = new ArrayList<>();
        Parameter[] methodParameters = method.getParameters();
        int cIndex = 0;
        for (int i = 0; i < methodParameters.length; i++) {
            Parameter parameter = methodParameters[i];
            AnnotationReader paramAnns = AnnotationReader.create(handler, parameter);
            List<ParameterValidator<Object>> validators = new ArrayList<>(
                    handler.validators.getFlexibleOrDefault(parameter.getType(), emptyList())
            );

            String[] defaultValue = paramAnns.get(Default.class, Default::value);
            if (defaultValue == null || defaultValue.length == 0 && paramAnns.contains(Optional.class))
                defaultValue = paramAnns.get(Optional.class, Optional::def);

            BaseCommandParameter param = new BaseCommandParameter(
                    getName(parameter),
                    paramAnns.get(Description.class, Description::value),
                    i,
                    defaultValue == null ? emptyList() : Collections.unmodifiableList(Arrays.asList(defaultValue)),
                    i == methodParameters.length - 1 && !paramAnns.contains(Single.class),
                    paramAnns.contains(Optional.class) || paramAnns.contains(Default.class),
                    command,
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

            /* Optional parmeters may be null, so make sure it isn't primitive as primitives cannot
               hold null values */
            if (param.getType().isPrimitive() && param.isOptional() && param.getDefaultValue().isEmpty() && !param.isSwitch())
                throw new IllegalStateException("Optional parameter " + parameter + " at " + method + " cannot be a prmitive!");

            /* Switches must only be booleans */
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

    /**
     * Concatenates all the paths for a command by merging those
     * defined by {@link Command}, {@link Subcommand}, and those
     * defined in the parent class, and any other parent classes
     *
     * @param container The class containing the commands
     * @param method    The method that contains annotations
     * @param reader    The annotation reader to read from
     * @return A list of all command paths that lead to this command
     */
    private static List<CommandPath> getCommandPath(@NotNull Class<?> container,
                                                    @NotNull Method method,
                                                    @NotNull AnnotationReader reader) {
        List<CommandPath> paths = new ArrayList<>();

        DefaultFor defaultFor = reader.get(DefaultFor.class);
        if (defaultFor != null) {
            return Arrays.stream(defaultFor.value()).map(CommandPath::parse)
                    .collect(Collectors.toList());
        }

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
                paths.add(CommandPath.parse(command));
            }
        }
        return paths;
    }

    /**
     * Returns a hierarchy of classes that are contained by the given class. The
     * order of the list matters, as it starts from the parent and ends at the child
     *
     * @param c Class to find for
     * @return A list of all classes containing each other
     */
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
