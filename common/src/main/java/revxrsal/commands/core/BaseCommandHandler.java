package revxrsal.commands.core;

import static revxrsal.commands.util.Preconditions.coerceIn;
import static revxrsal.commands.util.Preconditions.notEmpty;
import static revxrsal.commands.util.Preconditions.notNull;
import static revxrsal.commands.util.Primitives.getType;
import static revxrsal.commands.util.Strings.splitBySpace;

import dev.demeng.pluginbase.locale.Translator;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.CommandHandlerVisitor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.annotation.dynamic.AnnotationReplacer;
import revxrsal.commands.autocomplete.AutoCompleter;
import revxrsal.commands.command.ArgumentParser;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.reflect.MethodCallerFactory;
import revxrsal.commands.exception.ArgumentParseException;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.exception.DefaultExceptionHandler;
import revxrsal.commands.exception.InvalidBooleanException;
import revxrsal.commands.exception.InvalidURLException;
import revxrsal.commands.exception.InvalidUUIDException;
import revxrsal.commands.exception.NumberNotInRangeException;
import revxrsal.commands.exception.ThrowableFromCommand;
import revxrsal.commands.help.CommandHelp;
import revxrsal.commands.help.CommandHelpWriter;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.OrphanRegistry;
import revxrsal.commands.orphan.Orphans;
import revxrsal.commands.process.CommandCondition;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ContextResolverFactory;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ParameterResolver.ParameterResolverContext;
import revxrsal.commands.process.ParameterValidator;
import revxrsal.commands.process.PermissionReader;
import revxrsal.commands.process.ResponseHandler;
import revxrsal.commands.process.SenderResolver;
import revxrsal.commands.process.ValueResolver;
import revxrsal.commands.process.ValueResolver.ValueResolverContext;
import revxrsal.commands.process.ValueResolverFactory;
import revxrsal.commands.util.ClassMap;
import revxrsal.commands.util.Primitives;
import revxrsal.commands.util.StackTraceSanitizer;

public class BaseCommandHandler implements CommandHandler {

    protected final Map<CommandPath, CommandExecutable> executables = new HashMap<>();
    protected final Map<CommandPath, BaseCommandCategory> categories = new HashMap<>();
    private final BaseCommandDispatcher dispatcher = new BaseCommandDispatcher(this);

    final LinkedList<ResolverFactory> factories = new LinkedList<>();
    final BaseAutoCompleter autoCompleter = new BaseAutoCompleter(this);
    final ClassMap<List<ParameterValidator<Object>>> validators = new ClassMap<>();
    final ClassMap<ResponseHandler<?>> responseHandlers = new ClassMap<>();
    final ClassMap<Supplier<?>> dependencies = new ClassMap<>();
    final List<SenderResolver> senderResolvers = new ArrayList<>();
    private final Set<PermissionReader> permissionReaders = new HashSet<>();
    final Map<Class<?>, Set<AnnotationReplacer<?>>> annotationReplacers = new ClassMap<>();
    private MethodCallerFactory methodCallerFactory = MethodCallerFactory.defaultFactory();
    private ArgumentParser argumentParser = ArgumentParser.QUOTES;
    private final WrappedExceptionHandler exceptionHandler = new WrappedExceptionHandler(DefaultExceptionHandler.INSTANCE);
    private StackTraceSanitizer sanitizer = StackTraceSanitizer.defaultSanitizer();
    String flagPrefix = "-", switchPrefix = "-";
    CommandHelpWriter<?> helpWriter;
    boolean failOnExtra = false;
    final List<CommandCondition> conditions = new ArrayList<>();
    private final Translator translator = Translator.create();

    public BaseCommandHandler() {
        registerContextResolverFactory(new SenderContextResolverFactory(senderResolvers));
        registerContextResolverFactory(DependencyResolverFactory.INSTANCE);
        registerValueResolver(int.class, ValueResolverContext::popInt);
        registerValueResolver(double.class, ValueResolverContext::popDouble);
        registerValueResolver(short.class, ValueResolverContext::popShort);
        registerValueResolver(byte.class, ValueResolverContext::popByte);
        registerValueResolver(long.class, ValueResolverContext::popLong);
        registerValueResolver(float.class, ValueResolverContext::popFloat);
        registerValueResolver(boolean.class, bool());
        registerValueResolver(String.class, ValueResolverContext::popForParameter);
        registerValueResolver(UUID.class, context -> {
            String value = context.pop();
            try {
                return UUID.fromString(value);
            } catch (Throwable t) {
                throw new InvalidUUIDException(context.parameter(), value);
            }
        });
        registerValueResolver(URL.class, context -> {
            String value = context.pop();
            try {
                return new URL(value);
            } catch (MalformedURLException e) {
                throw new InvalidURLException(context.parameter(), value);
            }
        });
        registerValueResolver(URI.class, context -> {
            String value = context.pop();
            try {
                return new URI(value);
            } catch (URISyntaxException e) {
                throw new InvalidURLException(context.parameter(), value);
            }
        });
        registerContextResolver(CommandHandler.class, context -> this);
        registerContextResolver(ExecutableCommand.class, context -> context.parameter().getDeclaringCommand());
        registerContextResolver(CommandActor.class, ParameterResolverContext::actor);
        registerContextResolver((Class) CommandHelp.class, new BaseCommandHelp.Resolver(this));
        setExceptionHandler(DefaultExceptionHandler.INSTANCE);
        registerCondition(CooldownCondition.INSTANCE);
        registerParameterValidator(Number.class, (value, parameter, actor) -> {
            Range range = parameter.getAnnotation(Range.class);
            if (range != null)
                if (value.doubleValue() > range.max() || value.doubleValue() < range.min())
                    throw new NumberNotInRangeException(actor, parameter, value, range.min(), range.max());
        });
        registerCondition((actor, command, arguments) -> command.checkPermission(actor));
        registerAnnotationReplacer(Description.class, new LocalesAnnotationReplacer(this));
        registerResponseHandler(String.class, (response, actor, command) -> {
            if (response != null) {
                actor.reply(response);
            }
        });
    }

    @Override
    public @NotNull CommandHandler register(@NotNull Object... commands) {
        for (Object command : commands) {
            notNull(command, "Command");
            if (command instanceof OrphanCommand) {
                throw new IllegalArgumentException("You cannot register an OrphanCommand directly! " +
                        "You must wrap it using Orphans.path(...).handler(OrphanCommand)");
            }
            if (command instanceof Orphans) {
                throw new IllegalArgumentException("You forgot to call .handler(OrphanCommand) in your Orphans.path(...)!");
            }
            if (command instanceof OrphanRegistry) {
                setDependencies(((OrphanRegistry) command).getHandler());
                CommandParser.parse(this, ((OrphanRegistry) command));
            } else {
                setDependencies(command);
                CommandParser.parse(this, command);
            }
        }
        for (BaseCommandCategory category : categories.values()) {
            CommandPath categoryPath = category.getPath().getCategoryPath();
            category.parent(categoryPath == null ? null : categories.get(categoryPath));
            findPermission(category.defaultAction);
        }
        for (CommandExecutable executable : executables.values()) {
            findPermission(executable);
        }
        return this;
    }

    @Override public @NotNull Locale getLocale() {
        return translator.getLocale();
    }

    @Override public void setLocale(@NotNull Locale locale) {
        translator.setLocale(locale);
    }

    @Override public @NotNull Translator getTranslator() {
        return translator;
    }

    private void findPermission(@Nullable CommandExecutable executable) {
        if (executable == null) return;
        if (!executable.permissionSet) {
            for (PermissionReader reader : permissionReaders) {
                CommandPermission p = reader.getPermission(executable);
                if (p != null) {
                    executable.permissionSet = true;
                    executable.setPermission(p);
                    return;
                }
            }
        }
    }

    public Set<PermissionReader> getPermissionReaders() {
        return permissionReaders;
    }

    @Override public @NotNull CommandHandler setMethodCallerFactory(@NotNull MethodCallerFactory factory) {
        notNull(factory, "method caller factory");
        methodCallerFactory = factory;
        return this;
    }

    @Override public @NotNull ArgumentParser getArgumentParser() {
        return argumentParser;
    }

    @Override public ArgumentStack parseArgumentsForCompletion(String... arguments) throws ArgumentParseException {
        String args = String.join(" ", arguments);
        if (args.isEmpty())
            return ArgumentStack.copy(EMPTY_TEXT);
        return argumentParser.parse(args);
    }

    @Override public ArgumentStack parseArguments(String... arguments) throws ArgumentParseException {
        String args = String.join(" ", arguments);
        if (args.isEmpty())
            return ArgumentStack.empty();
        return argumentParser.parse(args);
    }

    /**
     * A collection that is returned for empty auto-completions.
     */
    private static final Collection<String> EMPTY_TEXT = Collections.singletonList("");

    @Override public BaseCommandHandler setArgumentParser(@NotNull ArgumentParser argumentParser) {
        notNull(argumentParser, "argument parser");
        this.argumentParser = argumentParser;
        return this;
    }

    @Override public @NotNull CommandHandler setExceptionHandler(@NotNull CommandExceptionHandler handler) {
        notNull(handler, "command exception handler");
        exceptionHandler.handler = handler;
        return this;
    }

    @Override public @NotNull <T extends Throwable> CommandHandler registerExceptionHandler(@NotNull Class<T> exceptionType,
                                                                                            @NotNull BiConsumer<CommandActor, T> handler) {
        notNull(exceptionType, "exception type");
        notNull(handler, "exception handler");
        exceptionHandler.exceptionsHandlers.add(exceptionType, (BiConsumer<CommandActor, Throwable>) handler);
        return this;
    }

    @Override public @NotNull CommandHandler setSwitchPrefix(@NotNull String prefix) {
        notNull(prefix, "prefix");
        notEmpty(prefix, "prefix cannot be empty!");
        switchPrefix = prefix;
        return this;
    }

    @Override public @NotNull CommandHandler setFlagPrefix(@NotNull String prefix) {
        notNull(prefix, "prefix");
        notEmpty(prefix, "prefix cannot be empty!");
        flagPrefix = prefix;
        return this;
    }

    @Override public @NotNull <T> CommandHandler setHelpWriter(@NotNull CommandHelpWriter<T> helpWriter) {
        notNull(helpWriter, "command help writer");
        this.helpWriter = helpWriter;
        return this;
    }

    @Override public @NotNull CommandHandler disableStackTraceSanitizing() {
        sanitizer = StackTraceSanitizer.empty();
        return this;
    }

    @Override public @NotNull CommandHandler failOnTooManyArguments() {
        failOnExtra = true;
        return this;
    }

    @Override public @NotNull CommandHandler registerSenderResolver(@NotNull SenderResolver resolver) {
        notNull(resolver, "resolver");
        senderResolvers.add(resolver);
        return this;
    }

    @Override public @NotNull CommandHandler registerPermissionReader(@NotNull PermissionReader reader) {
        notNull(reader, "permission reader");
        permissionReaders.add(reader);
        return this;
    }

    @Override public <T> @NotNull CommandHandler registerValueResolver(@NotNull Class<T> type, @NotNull ValueResolver<T> resolver) {
        notNull(type, "type");
        notNull(resolver, "resolver");
        if (type.isPrimitive())
            registerValueResolver(Primitives.wrap(type), resolver);
        factories.add(new ResolverFactory(ValueResolverFactory.forType(type, resolver)));
        return this;
    }

    @Override public @NotNull <T> CommandHandler registerValueResolver(int priority, @NotNull Class<T> type, @NotNull ValueResolver<T> resolver) {
        notNull(type, "type");
        notNull(resolver, "resolver");
        if (type.isPrimitive())
            registerValueResolver(priority, Primitives.wrap(type), resolver);
        factories.add(coerceIn(priority, 0, factories.size()), new ResolverFactory(ValueResolverFactory.forType(type, resolver)));
        return this;
    }

    @Override public <T> @NotNull CommandHandler registerContextResolver(@NotNull Class<T> type, @NotNull ContextResolver<T> resolver) {
        notNull(type, "type");
        notNull(resolver, "resolver");
        if (type.isPrimitive())
            registerContextResolver(Primitives.wrap(type), resolver);
        factories.add(new ResolverFactory(ContextResolverFactory.forType(type, resolver)));
        return this;
    }

    @Override public @NotNull <T> CommandHandler registerContextResolver(int priority, @NotNull Class<T> type, @NotNull ContextResolver<T> resolver) {
        notNull(type, "type");
        notNull(resolver, "resolver");
        if (type.isPrimitive())
            registerContextResolver(Primitives.wrap(type), resolver);
        factories.add(coerceIn(priority, 0, factories.size()), new ResolverFactory(ContextResolverFactory.forType(type, resolver)));
        return this;
    }

    @Override public @NotNull <T> CommandHandler registerContextValue(@NotNull Class<T> type, T value) {
        return registerContextResolver(type, ContextResolver.of(value));
    }

    @Override public @NotNull <T> CommandHandler registerContextValue(int priority, @NotNull Class<T> type, @NotNull T value) {
        return registerContextResolver(priority, type, ContextResolver.of(value));
    }

    @Override public @NotNull CommandHandler registerValueResolverFactory(@NotNull ValueResolverFactory factory) {
        notNull(factory, "value resolver factory");
        factories.add(new ResolverFactory(factory));
        return this;
    }

    @Override public @NotNull CommandHandler registerValueResolverFactory(int priority, @NotNull ValueResolverFactory factory) {
        notNull(factory, "value resolver factory");
        factories.add(coerceIn(priority, 0, factories.size()), new ResolverFactory(factory));
        return this;
    }

    @Override public @NotNull CommandHandler registerContextResolverFactory(@NotNull ContextResolverFactory factory) {
        notNull(factory, "context resolver factory");
        factories.add(new ResolverFactory(factory));
        return this;
    }

    @Override public @NotNull CommandHandler registerContextResolverFactory(int priority, @NotNull ContextResolverFactory factory) {
        notNull(factory, "context resolver factory");
        factories.add(coerceIn(priority, 0, factories.size()), new ResolverFactory(factory));
        return this;
    }

    @Override public @NotNull CommandHandler registerCondition(@NotNull CommandCondition condition) {
        notNull(condition, "condition");
        conditions.add(condition);
        return this;
    }

    @Override public @NotNull <T> CommandHandler registerDependency(@NotNull Class<T> type, @NotNull Supplier<T> supplier) {
        notNull(type, "type");
        notNull(supplier, "supplier");
        dependencies.add(type, supplier);
        return this;
    }

    @Override public @NotNull <T> CommandHandler registerDependency(@NotNull Class<T> type, T value) {
        notNull(type, "type");
        dependencies.add(type, () -> value);
        return this;
    }

    @Override public @NotNull <T> CommandHandler registerParameterValidator(@NotNull Class<T> type, @NotNull ParameterValidator<T> validator) {
        notNull(type, "type");
        notNull(validator, "validator");
        validators.computeIfAbsent(Primitives.wrap(type), t -> new ArrayList<>()).add((ParameterValidator<Object>) validator);
        return this;
    }

    @Override public @NotNull <T> CommandHandler registerResponseHandler(@NotNull Class<T> responseType, @NotNull ResponseHandler<T> handler) {
        notNull(responseType, "response type");
        notNull(handler, "response handler");
        responseHandlers.add(responseType, handler);
        return this;
    }

    @Override public @NotNull <T extends Annotation> CommandHandler registerAnnotationReplacer(@NotNull Class<T> annotationType, @NotNull AnnotationReplacer<T> replacer) {
        notNull(annotationType, "annotation type");
        notNull(replacer, "annotation replacer");
        annotationReplacers.computeIfAbsent(annotationType, e -> new HashSet<>()).add(replacer);
        return this;
    }

    @Override public @NotNull CommandHandler accept(@NotNull CommandHandlerVisitor visitor) {
        notNull(visitor, "command handler visitor cannot be null!");
        visitor.visit(this);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public @Nullable <T extends Annotation> List<Annotation> replaceAnnotation(AnnotatedElement element, T ann) {
        Set<AnnotationReplacer<?>> replacers = annotationReplacers.get(ann.annotationType());
        if (replacers == null || replacers.isEmpty()) return null;
        List<Annotation> annotations = new ArrayList<>();
        for (AnnotationReplacer replacer : replacers) {
            Collection<Annotation> replaced = replacer.replaceAnnotations(element, ann);
            if (replaced == null || replaced.isEmpty()) continue;
            annotations.addAll(replaced);
        }
        if (annotations.isEmpty()) return null;
        return annotations;
    }

    @Override public @NotNull AutoCompleter getAutoCompleter() {
        return autoCompleter;
    }

    @Override public ExecutableCommand getCommand(@NotNull CommandPath path) {
        return executables.get(path);
    }

    @Override public CommandCategory getCategory(@NotNull CommandPath path) {
        return categories.get(path);
    }

    @Override public @UnmodifiableView @NotNull Map<CommandPath, ExecutableCommand> getCommands() {
        return Collections.unmodifiableMap(executables);
    }

    @Override public @UnmodifiableView @NotNull Map<CommandPath, CommandCategory> getCategories() {
        return Collections.unmodifiableMap(categories);
    }

    public <T> ParameterResolver<T> getResolver(CommandParameter parameter) {
        ParameterResolver<T> cached = null;

        for (ResolverFactory factory : factories) {
            Resolver resolver = factory.create(parameter);
            if (resolver == null) continue;
            return (ParameterResolver<T>) resolver;
        }
        if (parameter.getType().isEnum()) {
            return (ParameterResolver<T>) new Resolver(null, EnumResolverFactory.INSTANCE.create(parameter));
        }
        return null;
    }

    @Override public @NotNull CommandExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    @Override public @NotNull MethodCallerFactory getMethodCallerFactory() {
        return methodCallerFactory;
    }

    @Override public <T> CommandHelpWriter<T> getHelpWriter() {
        return (CommandHelpWriter<T>) helpWriter;
    }

    private void unregister(CommandPath path, CommandExecutable command) {
        BaseCommandCategory parent = command.parent;
        if (parent != null) {
            parent.commands.remove(path);
            if (parent.isEmpty()) categories.remove(parent.path);
        }
    }

    private void unregister(CommandPath path, BaseCommandCategory category) {
        BaseCommandCategory parent = category.parent;
        if (parent != null) {
            parent.commands.remove(path);
            if (parent.isEmpty()) categories.remove(parent.path);
        }
    }

    @Override public boolean unregister(@NotNull CommandPath path) {
        boolean modified = false;
        for (Iterator<Entry<CommandPath, CommandExecutable>> iterator = executables.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<CommandPath, CommandExecutable> entry = iterator.next();
            if (entry.getKey().isChildOf(path)) {
                modified = true;
                iterator.remove();
                unregister(path, entry.getValue());
            }
        }
        for (Iterator<Entry<CommandPath, BaseCommandCategory>> iterator = categories.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<CommandPath, BaseCommandCategory> entry = iterator.next();
            if (entry.getKey().isChildOf(path)) {
                modified = true;
                iterator.remove();
                unregister(path, entry.getValue());
            }
        }
        return modified;
    }

    @Override public boolean unregister(@NotNull String commandPath) {
        return unregister(CommandPath.get(splitBySpace(commandPath)));
    }

    @Override public void unregisterAllCommands() {
        // it's important that we don't just do a blind executables.clear()
        // or categories.clear(), since some platforms register commands
        // in their own way (such as Bukkit).
        getRootPaths().forEach(this::unregister);
    }

    @Override public @NotNull Set<CommandPath> getRootPaths() {
        Set<CommandPath> paths = new HashSet<>();
        for (CommandPath path : categories.keySet()) if (path.isRoot()) paths.add(path);
        for (CommandPath path : executables.keySet()) if (path.isRoot()) paths.add(path);
        return paths;
    }

    @Override public @NotNull String getSwitchPrefix() {
        return switchPrefix;
    }

    @Override public @NotNull String getFlagPrefix() {
        return flagPrefix;
    }

    @Override public <T> @NotNull Optional<@Nullable T> dispatch(@NotNull CommandActor actor, @NotNull ArgumentStack arguments) {
        return (Optional<T>) Optional.ofNullable(dispatcher.eval(actor, arguments));
    }

    @Override public <T> @NotNull Optional<@Nullable T> dispatch(@NotNull CommandActor actor, @NotNull String commandInput) {
        try {
            return dispatch(actor, parseArguments(commandInput));
        } catch (Throwable t) {
            getExceptionHandler().handleException(t, actor);
            return Optional.empty();
        }
    }

    @Override public <T> Supplier<T> getDependency(@NotNull Class<T> dependencyType) {
        return (Supplier<T>) dependencies.getFlexible(dependencyType);
    }

    @Override public <T> Supplier<T> getDependency(@NotNull Class<T> dependencyType, Supplier<T> def) {
        return (Supplier<T>) dependencies.getFlexibleOrDefault(dependencyType, def);
    }

    protected void setDependencies(Object ob) {
        for (Field field : getType(ob).getDeclaredFields()) {
            if (!field.isAnnotationPresent(Dependency.class)) continue;
            if (!field.isAccessible()) field.setAccessible(true);
            Supplier<?> dependency = dependencies.getFlexible(field.getType());
            if (dependency == null) {
                throw new IllegalStateException("Unable to find correct dependency for type " + field.getType());
            }
            try {
                field.set(ob, dependency.get());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to inject dependency value into field " + field.getName(), e);
            }
        }
    }

    private ValueResolver<Boolean> bool() {
        return context -> {
            String v = context.pop();
            switch (v.toLowerCase()) {
                case "true":
                case "yes":
                case "ye":
                case "y":
                case "yeah":
                case "ofcourse":
                case "mhm":
                    return true;
                case "false":
                case "no":
                case "n":
                    return false;
                default:
                    throw new InvalidBooleanException(context.parameter(), v);
            }
        };
    }

    private class WrappedExceptionHandler implements CommandExceptionHandler {

        private final ClassMap<BiConsumer<CommandActor, Throwable>> exceptionsHandlers = new ClassMap<>();
        private @NotNull CommandExceptionHandler handler;

        public WrappedExceptionHandler(@NotNull CommandExceptionHandler handler) {
            this.handler = handler;
        }

        @Override public void handleException(@NotNull Throwable throwable, @NotNull CommandActor actor) {
            Throwable cause = throwable.getCause();
            if (cause != null && (cause.getClass().isAnnotationPresent(ThrowableFromCommand.class) ||
                    exceptionsHandlers.getFlexible(cause.getClass()) != null)
            ) {
                throwable = cause;
            }
            @Nullable BiConsumer<CommandActor, Throwable> registered = exceptionsHandlers.getFlexible(throwable.getClass());
            sanitizer.sanitize(throwable);
            if (registered != null) {
                registered.accept(actor, throwable);
                return;
            }
            handler.handleException(throwable, actor);
        }
    }

}
