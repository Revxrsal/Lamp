package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.CommandHandlerVisitor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.autocomplete.AutoCompleter;
import revxrsal.commands.command.*;
import revxrsal.commands.core.reflect.MethodCallerFactory;
import revxrsal.commands.exception.*;
import revxrsal.commands.help.CommandHelp;
import revxrsal.commands.help.CommandHelpWriter;
import revxrsal.commands.process.*;
import revxrsal.commands.process.ParameterResolver.ParameterResolverContext;
import revxrsal.commands.process.ValueResolver.ValueResolverContext;
import revxrsal.commands.util.ClassMap;
import revxrsal.commands.util.Primitives;
import revxrsal.commands.util.StackTraceSanitizer;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;

import static revxrsal.commands.util.Preconditions.notEmpty;
import static revxrsal.commands.util.Preconditions.notNull;
import static revxrsal.commands.util.Primitives.getType;
import static revxrsal.commands.util.Strings.splitBySpace;

public class BaseCommandHandler implements CommandHandler {

    protected final Map<CommandPath, CommandExecutable> executables = new HashMap<>();
    protected final Map<CommandPath, BaseCommandCategory> categories = new HashMap<>();
    private final BaseCommandDispatcher dispatcher = new BaseCommandDispatcher(this);

    final List<ResolverFactory> factories = new ArrayList<>();
    final BaseAutoCompleter autoCompleter = new BaseAutoCompleter(this);
    final ClassMap<Resolver> resolversCache = new ClassMap<>();
    final ClassMap<List<ParameterValidator<Object>>> validators = new ClassMap<>();
    final ClassMap<ResponseHandler<?>> responseHandlers = new ClassMap<>();
    final ClassMap<Supplier<?>> dependencies = new ClassMap<>();
    final List<SenderResolver> senderResolvers = new ArrayList<>();
    private final Set<PermissionReader> permissionReaders = new HashSet<>();
    private MethodCallerFactory methodCallerFactory = MethodCallerFactory.defaultFactory();
    private final WrappedExceptionHandler exceptionHandler = new WrappedExceptionHandler(DefaultExceptionHandler.INSTANCE);
    private StackTraceSanitizer sanitizer = StackTraceSanitizer.defaultSanitizer();
    String flagPrefix = "-", switchPrefix = "-";
    CommandHelpWriter<?> helpWriter;
    boolean failOnExtra = false;
    final List<CommandCondition> conditions = new ArrayList<>();

    public BaseCommandHandler() {
        registerContextResolverFactory(new SenderContextResolverFactory(senderResolvers));
        registerContextResolverFactory(DependencyResolverFactory.INSTANCE);
        registerValueResolverFactory(EnumResolverFactory.INSTANCE);
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
                return new URL(context.pop());
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
        registerCondition((actor, command, arguments) -> {
            if (!command.getPermission().canExecute(actor))
                throw new NoPermissionException(command, command.getPermission());
        });
    }

    @Override
    public CommandHandler register(@NotNull Object... commands) {
        for (Object command : commands) {
            notNull(command, "Command");
            setDependencies(command);
            CommandParser.parse(this, command);
        }
        for (BaseCommandCategory category : categories.values()) {
            CommandPath categoryPath = category.getPath().getCategoryPath();
            category.parent(categoryPath == null ? null : categories.get(categoryPath));
        }
        command_loop:
        for (CommandExecutable executable : executables.values()) {
            if (!executable.permissionSet) {
                for (PermissionReader reader : permissionReaders) {
                    CommandPermission p = reader.getPermission(executable);
                    if (p != null) {
                        executable.permissionSet = true;
                        executable.setPermission(p);
                        continue command_loop;
                    }
                }
            }
        }
        return this;
    }

    @Override public CommandHandler setMethodCallerFactory(@NotNull MethodCallerFactory factory) {
        notNull(factory, "method caller factory");
        methodCallerFactory = factory;
        return this;
    }

    @Override public CommandHandler setExceptionHandler(@NotNull CommandExceptionHandler handler) {
        notNull(handler, "command exception handler");
        exceptionHandler.handler = handler;
        return this;
    }

    @Override public CommandHandler setSwitchPrefix(@NotNull String prefix) {
        notNull(prefix, "prefix");
        notEmpty(prefix, "prefix cannot be empty!");
        switchPrefix = prefix;
        return this;
    }

    @Override public CommandHandler setFlagPrefix(@NotNull String prefix) {
        notNull(prefix, "prefix");
        notEmpty(prefix, "prefix cannot be empty!");
        flagPrefix = prefix;
        return this;
    }

    @Override public <T> CommandHandler setHelpWriter(@NotNull CommandHelpWriter<T> helpWriter) {
        notNull(helpWriter, "command help writer");
        this.helpWriter = helpWriter;
        return this;
    }

    @Override public CommandHandler disableStackTraceSanitizing() {
        sanitizer = StackTraceSanitizer.empty();
        return this;
    }

    @Override public CommandHandler failOnTooManyArguments() {
        failOnExtra = true;
        return this;
    }

    @Override public CommandHandler registerSenderResolver(@NotNull SenderResolver resolver) {
        notNull(resolver, "resolver");
        senderResolvers.add(resolver);
        return this;
    }

    @Override public CommandHandler registerPermissionReader(@NotNull PermissionReader reader) {
        notNull(reader, "permission reader");
        permissionReaders.add(reader);
        return this;
    }

    @Override public <T> CommandHandler registerValueResolver(@NotNull Class<T> type, @NotNull ValueResolver<T> resolver) {
        notNull(type, "type");
        notNull(resolver, "resolver");
        if (type.isPrimitive())
            registerValueResolver(Primitives.wrap(type), resolver);
        factories.add(new ResolverFactory(ValueResolverFactory.forType(type, resolver)));
        return this;
    }

    @Override public <T> CommandHandler registerContextResolver(@NotNull Class<T> type, @NotNull ContextResolver<T> resolver) {
        notNull(type, "type");
        notNull(resolver, "resolver");
        factories.add(new ResolverFactory(ContextResolverFactory.forType(type, resolver)));
        return this;
    }

    @Override public <T> CommandHandler registerContextValue(@NotNull Class<T> type, T value) {
        return registerContextResolver(type, ContextResolver.of(value));
    }

    @Override public CommandHandler registerValueResolverFactory(@NotNull ValueResolverFactory factory) {
        notNull(factory, "value resolver factory");
        factories.add(new ResolverFactory(factory));
        return this;
    }

    @Override public CommandHandler registerContextResolverFactory(@NotNull ContextResolverFactory factory) {
        notNull(factory, "context resolver factory");
        factories.add(new ResolverFactory(factory));
        return this;
    }

    @Override public CommandHandler registerCondition(@NotNull CommandCondition condition) {
        notNull(condition, "condition");
        conditions.add(condition);
        return this;
    }

    @Override public <T> CommandHandler registerDependency(@NotNull Class<T> type, @NotNull Supplier<T> supplier) {
        notNull(type, "type");
        notNull(supplier, "supplier");
        dependencies.add(type, supplier);
        return this;
    }

    @Override public <T> CommandHandler registerDependency(@NotNull Class<T> type, T value) {
        notNull(type, "type");
        dependencies.add(type, () -> value);
        return this;
    }

    @Override public <T> CommandHandler registerParameterValidator(@NotNull Class<T> type, @NotNull ParameterValidator<T> validator) {
        notNull(type, "type");
        notNull(validator, "validator");
        validators.computeIfAbsent(Primitives.wrap(type), t -> new ArrayList<>()).add((ParameterValidator<Object>) validator);
        return this;
    }

    @Override public <T> CommandHandler registerResponseHandler(@NotNull Class<T> responseType, @NotNull ResponseHandler<T> handler) {
        notNull(responseType, "response type");
        notNull(handler, "response handler");
        responseHandlers.add(responseType, handler);
        return this;
    }

    @Override public CommandHandler accept(@NotNull CommandHandlerVisitor visitor) {
        notNull(visitor, "command handler visitor cannot be null!");
        visitor.visit(this);
        return this;
    }

    @Override public AutoCompleter getAutoCompleter() {
        return autoCompleter;
    }

    @Override public ExecutableCommand getCommand(@NotNull CommandPath path) {
        return executables.get(path);
    }

    @Override public CommandCategory getCategory(@NotNull CommandPath path) {
        return categories.get(path);
    }

    @Override public @UnmodifiableView Map<CommandPath, ExecutableCommand> getCommands() {
        return Collections.unmodifiableMap(executables);
    }

    @Override public @UnmodifiableView Map<CommandPath, CommandCategory> getCategories() {
        return Collections.unmodifiableMap(categories);
    }

    public <T> ParameterResolver<T> getResolver(CommandParameter parameter) {
        ParameterResolver<T> cached = (ParameterResolver<T>) resolversCache.get(parameter.getType());
        if (cached != null) return cached;

        for (ResolverFactory factory : factories) {
            Resolver resolver = factory.create(parameter);
            if (resolver == null) continue;
            cached = (ParameterResolver<T>) resolver;
            resolversCache.add(parameter.getType(), (Resolver) cached);
            break;
        }

        return cached;
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

    @Override public boolean unregister(@NotNull CommandPath path) {
        boolean modified;
        modified = categories.keySet().removeIf(c -> c.isChildOf(path));
        modified |= executables.keySet().removeIf(c -> c.isChildOf(path));
        return modified;
    }

    @Override public boolean unregister(@NotNull String commandPath) {
        return unregister(CommandPath.get(splitBySpace(commandPath)));
    }

    @Override public String getSwitchPrefix() {
        return switchPrefix;
    }

    @Override public String getFlagPrefix() {
        return flagPrefix;
    }

    @Override public <T> @NotNull Optional<@Nullable T> dispatch(@NotNull CommandActor actor, @NotNull ArgumentStack arguments) {
        return (Optional<T>) Optional.ofNullable(dispatcher.eval(actor, arguments));
    }

    @Override public void dispatch(@NotNull CommandActor actor, @NotNull String commandInput) {
        dispatch(actor, ArgumentStack.fromString(commandInput));
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

        private @NotNull CommandExceptionHandler handler;

        public WrappedExceptionHandler(@NotNull CommandExceptionHandler handler) {
            this.handler = handler;
        }

        @Override public void handleException(@NotNull Throwable throwable, @NotNull CommandActor actor) {
            Throwable cause = throwable.getCause();
            if (cause != null && cause.getClass().isAnnotationPresent(ThrowableFromCommand.class)) {
                throwable = cause;
            }
            sanitizer.sanitize(throwable);
            handler.handleException(throwable, actor);
        }
    }

}
