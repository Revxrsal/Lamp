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
package revxrsal.commands;

import org.jetbrains.annotations.*;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.dynamic.AnnotationReplacer;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.autocomplete.AutoCompleter;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.autocomplete.SuggestionProviders;
import revxrsal.commands.command.*;
import revxrsal.commands.exception.*;
import revxrsal.commands.exception.context.ErrorContext;
import revxrsal.commands.hook.Hooks;
import revxrsal.commands.ktx.KotlinFeatureRegistry;
import revxrsal.commands.node.CommandRegistry;
import revxrsal.commands.node.DispatcherSettings;
import revxrsal.commands.node.ParameterNamingStrategy;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.node.parser.BaseCommandRegistry;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.OrphanRegistry;
import revxrsal.commands.orphan.Orphans;
import revxrsal.commands.parameter.*;
import revxrsal.commands.process.CommandCondition;
import revxrsal.commands.process.MessageSender;
import revxrsal.commands.process.ParameterValidator;
import revxrsal.commands.process.SenderResolver;
import revxrsal.commands.response.CompletionStageResponseHandler;
import revxrsal.commands.response.OptionalResponseHandler;
import revxrsal.commands.response.ResponseHandler;
import revxrsal.commands.response.SupplierResponseHandler;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static revxrsal.commands.util.Classes.checkRetention;
import static revxrsal.commands.util.Classes.wrap;
import static revxrsal.commands.util.Collections.copyList;
import static revxrsal.commands.util.Collections.copyMap;
import static revxrsal.commands.util.Preconditions.notNull;

/**
 * The main entrypoint to command registration. Instances should
 * be created with {@link #builder()}.
 * <p>
 * Lamp instances are expensive to create. They are immutable so
 * they are safe to re-use and share across threads.
 *
 * @param <A> The actor type
 */
public final class Lamp<A extends CommandActor> {

    private final Map<Class<? extends Annotation>, Set<AnnotationReplacer<?>>> annotationReplacers;
    private final ParameterNamingStrategy parameterNamingStrategy;
    private final ParameterTypes<A> parameterTypes;
    private final SuggestionProviders<A> suggestionProviders;
    private final Hooks<A> hooks;
    private final List<SenderResolver<? super A>> senderResolvers;
    private final List<ParameterValidator<A, Object>> validators;
    private final List<CommandCondition<? super A>> commandConditions;
    private final List<ResponseHandler.Factory<? super A>> responseHandlers;
    private final List<CommandPermission.Factory<? super A>> permissionFactories;
    private final MessageSender<? super A, String> messageSender, errorSender;
    private final Map<Class<?>, Supplier<Object>> dependencies;
    private final CommandExceptionHandler<A> exceptionHandler;
    private final DispatcherSettings<A> dispatcherSettings;
    private final BaseCommandRegistry<A> tree;
    private final AutoCompleter<A> autoCompleter;

    @SuppressWarnings("unchecked")
    public Lamp(Builder<A> builder) {
        this.annotationReplacers = copyMap(builder.annotationReplacers);
        this.senderResolvers = copyList(builder.senderResolvers);
        this.validators = copyList(builder.validators);
        this.responseHandlers = copyList(builder.responseHandlers);
        this.commandConditions = copyList(builder.conditions);
        //noinspection rawtypes
        this.permissionFactories = (List) copyList(builder.permissionFactories);
        this.dependencies = copyMap(builder.dependencies);
        this.messageSender = builder.messageSender;
        this.errorSender = builder.errorSender;
        this.parameterNamingStrategy = builder.namingStrategy;
        this.parameterTypes = builder.parameterTypes.build();
        this.suggestionProviders = builder.suggestionProviders.build();
        this.hooks = builder.hooks.build();
        this.exceptionHandler = builder.exceptionHandler;
        this.dispatcherSettings = builder.dispatcherSettings.build();
        this.tree = new BaseCommandRegistry<>(this);
        this.autoCompleter = AutoCompleter.create(this);
    }

    /**
     * Creates a new Lamp {@link Builder}
     *
     * @param <A> The actor type
     * @return The newly created builder
     */
    public static <A extends CommandActor> @NotNull Builder<A> builder() {
        return new Builder<>();
    }

    /**
     * Returns the first {@link ParameterType} that can parse the given parameter.
     * <p>
     * Note that this method will never return {@code null}. In cases where no
     * suitable factory is found, it will throw {@link IllegalArgumentException}.
     *
     * @param parameter The parameter to create a resolver for
     * @return The parameter type
     * @throws IllegalStateException if no suitable parameter type was found
     */
    public <T> @NotNull ParameterResolver<A, T> resolver(@NotNull CommandParameter parameter) {
        return resolver(parameter.fullType(), parameter.annotations());
    }

    /**
     * Returns the first {@link ParameterType} that can parse the given type.
     * This method can be used to create more complex parameter types for
     * composite types (such as {@code T[]}).
     * <p>
     * Note that this method assumes the type has no annotations.
     * <p>
     * Note that this method will never return {@code null}. In cases where no
     * suitable factory is found, it will throw {@link IllegalArgumentException}.
     *
     * @param type The type to create for
     * @return The parameter type
     * @throws IllegalStateException if no suitable parameter type was found
     */
    public <T> @NotNull ParameterResolver<A, T> resolver(@NotNull Type type) {
        return resolver(type, AnnotationList.empty());
    }

    /**
     * Returns the first {@link ParameterType} that can parse the given type
     * and annotations. This method can be used to create more complex parameter
     * types for composite types (such as {@code T[]})
     * <p>
     * Note that this method will never return {@code null}. In cases where no
     * suitable factory is found, it will throw {@link IllegalArgumentException}.
     *
     * @param type        The type to create for
     * @param annotations The annotations to pass to factories
     * @return The parameter type
     * @throws IllegalStateException if no suitable parameter type was found
     */
    public <T> @NotNull ParameterResolver<A, T> resolver(@NotNull Type type, @NotNull AnnotationList annotations) {
        return parameterTypes.resolver(type, annotations, this);
    }

    /**
     * Returns the first {@link ParameterType} that comes after the {@code skipPast}
     * factory. This is useful for adding behavior on top of existing factory.
     *
     * @param type        The type to create for
     * @param annotations The annotations to pass to factories
     * @param skipPast    The factory to skip past. In most cases, this will be the caller factory,
     *                    i.e. {@code this}.
     * @return The parameter type
     * @throws IllegalStateException if no suitable parameter type was found
     */
    public <T> @NotNull ParameterResolver<A, T> findNextResolver(
            @NotNull Type type,
            @NotNull AnnotationList annotations,
            @NotNull ParameterFactory skipPast
    ) {
        return parameterTypes.findNextResolver(type, annotations, skipPast, this);
    }

    /**
     * Returns the first {@link SuggestionProvider} that can create suggestions
     * for the given type and annotations. This method can be used
     * to create more complex suggestion providers for composite types (such as {@code T[]})
     * <p>
     * Note that this method assumes the type has no annotations.
     * <p>
     * Note that this method will never return {@code null}. In cases where no
     * suitable provider is found, it will return {@link SuggestionProvider#empty()}.
     *
     * @param type The type to create for
     * @return The suggestion provider, or {@link SuggestionProvider#empty()}.
     */
    public @NotNull SuggestionProvider<A> suggestionProvider(Type type) {
        return suggestionProvider(type, AnnotationList.empty());
    }

    /**
     * Returns the first {@link SuggestionProvider} that can create suggestions
     * for the given type and annotations. This method can be used
     * to create more complex suggestion providers for composite types (such as {@code T[]})
     * <p>
     * Note that this method will never return {@code null}. In cases where no
     * suitable provider is found, it will return {@link SuggestionProvider#empty()}.
     *
     * @param type        The type to create for
     * @param annotations The annotations to pass to factories
     * @return The suggestion provider, or {@link SuggestionProvider#empty()}.
     */
    public @NotNull SuggestionProvider<A> suggestionProvider(Type type, AnnotationList annotations) {
        return suggestionProviders.provider(type, annotations, this);
    }

    /**
     * Returns the first {@link SuggestionProvider} that can create suggestions
     * for the given parameter.
     * <p>
     * Note that this method will never return {@code null}. In cases
     * where no suitable provider is found, it will return {@link SuggestionProvider#empty()}.
     *
     * @param parameter The parameter to create for
     * @return The suggestion provider, or {@link SuggestionProvider#empty()}.
     */
    public @NotNull SuggestionProvider<A> suggestionProvider(CommandParameter parameter) {
        return suggestionProviders.provider(parameter, this);
    }

    /**
     * Returns the first {@link SuggestionProvider} that comes after the {@code skipPast}
     * factory. This is useful for adding behavior on top of existing providers.
     *
     * @param type        The type to create for
     * @param annotations The annotations to pass to factories
     * @param skipPast    The factory to skip past. In most cases, this will be the caller factory,
     *                    i.e. {@code this}.
     * @param lamp        The {@link Lamp} instance to pass to factories
     * @return The suggestion provider, or {@link SuggestionProvider#empty()}.
     */
    public SuggestionProvider<A> findNextSuggestionProvider(Type type, AnnotationList annotations, SuggestionProvider.Factory<? super A> skipPast, Lamp<A> lamp) {
        return suggestionProviders.findNextProvider(type, annotations, skipPast, lamp);
    }

    /**
     * Returns the first {@link ResponseHandler} that can handle the given type
     * and annotation list.
     * <p>
     * Note that this method will never return {@code null}. In cases
     * where no suitable provider is found, it will return {@link ResponseHandler#noOp()}.
     *
     * @param type        The type to create for
     * @param annotations The annotations to pass to factories
     * @return The suggestion provider, or {@link SuggestionProvider#empty()}.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> @NotNull ResponseHandler<A, T> responseHandler(@NotNull Type type, @NotNull AnnotationList annotations) {
        for (ResponseHandler.Factory<? super A> responseHandler : responseHandlers) {
            ResponseHandler<A, Object> handler = responseHandler.create(type, annotations, ((Lamp) this));
            if (handler != null)
                return (ResponseHandler<A, T>) handler;
        }
        return ResponseHandler.noOp();
    }

    /**
     * Registers the given instance to the command handler. This
     * can deal with {@link Orphans orphan commands} as well as {@link Class} objects.
     *
     * @param instances The instance to register
     * @return The newly registered commands (as an immutable list).
     */
    public @NotNull @Unmodifiable List<ExecutableCommand<A>> register(Object... instances) {
        List<ExecutableCommand<A>> registered = new ArrayList<>();
        for (Object instance : instances) {
            Class<?> commandClass = instance instanceof Class ? (Class<?>) instance : instance.getClass();
            if (instance instanceof OrphanCommand) {
                throw new IllegalArgumentException("You cannot register an OrphanCommand directly! " +
                        "You must wrap it using Orphans.path(...).handler(OrphanCommand)");
            }
            if (instance instanceof Orphans) {
                throw new IllegalArgumentException("You forgot to call .handler(OrphanCommand) in your Orphans.path(...)!");
            }
            if (instance instanceof OrphanRegistry) {
                OrphanRegistry registry = (OrphanRegistry) instance;
                commandClass = registry.handler().getClass();
                instance = registry.handler();
                return tree.register(commandClass, instance, registry.paths());
            }

            registered.addAll(tree.register(commandClass, instance));
        }
        return registered;
    }

    /**
     * Unregisters the given executable command
     *
     * @param execution The command to unregister.
     */
    public void unregister(@NotNull ExecutableCommand<A> execution) {
        if (hooks.onCommandUnregistered(execution))
            tree.unregister(execution);
    }

    /**
     * Unregisters all the commands in this Lamp instance
     */
    public void unregisterAllCommands() {
        tree.unregisterIf(hooks::onCommandUnregistered);
    }

    /**
     * Unregisters all the commands in this Lamp instance that match
     * the given predicate
     */
    public void unregisterIf(@NotNull Predicate<ExecutableCommand<A>> commandPredicate) {
        tree.unregisterIf(command -> commandPredicate.test(command) && hooks.onCommandUnregistered(command));
    }

    /**
     * Executes the given input on the behalf of the specified actor.
     *
     * @param actor The actor to send for
     * @param input The input to execute with
     */
    public void dispatch(@NotNull A actor, String input) {
        MutableStringStream stream = StringStream.createMutable(input);
        tree.execute(actor, stream);
    }

    /**
     * Executes the given input on the behalf of the specified actor.
     *
     * @param actor The actor to send for
     * @param input The input to execute with
     */
    public void dispatch(@NotNull A actor, StringStream input) {
        MutableStringStream stream = input.isMutable() ? ((MutableStringStream) input) : input.toMutableCopy();
        tree.execute(actor, stream);
    }

    /**
     * Returns the command registry.
     *
     * @return The command registry
     */
    public @NotNull CommandRegistry<A> registry() {
        return tree;
    }

    /**
     * Returns an immutable map of the annotation replacers registered
     * in this {@link Lamp} instance
     *
     * @return The annotation replacers
     * @see Builder#annotationReplacer(Class, AnnotationReplacer)
     */
    public @Unmodifiable @NotNull Map<Class<? extends Annotation>, Set<AnnotationReplacer<?>>> annotationReplacers() {
        return annotationReplacers;
    }

    /**
     * Returns an immutable list of the command conditions registered
     * in this {@link Lamp} instance
     *
     * @return The command conditions
     * @see Builder#commandCondition(CommandCondition)
     */
    public @Unmodifiable @NotNull List<CommandCondition<? super A>> commandConditions() {
        return commandConditions;
    }

    /**
     * Returns the parameter naming strategy for this {@link Lamp} instance
     *
     * @return The parameter naming strategy
     * @see Builder#parameterNamingStrategy(ParameterNamingStrategy)
     */
    public @NotNull ParameterNamingStrategy parameterNamingStrategy() {
        return parameterNamingStrategy;
    }

    /**
     * Returns the parameter type registry
     *
     * @return The parameter type registry
     * @see Builder#parameterTypes()
     */
    public @NotNull ParameterTypes<A> parameterTypes() {
        return parameterTypes;
    }

    /**
     * Returns the suggestion provider registry
     *
     * @return The suggestion provider registry
     * @see Builder#suggestionProviders()
     */
    public @NotNull SuggestionProviders<A> suggestionProviders() {
        return suggestionProviders;
    }

    /**
     * Returns the hooks registry
     *
     * @return The hooks registry
     * @see Builder#hooks()
     */
    public @NotNull Hooks<A> hooks() {
        return hooks;
    }

    /**
     * Returns an immutable list of the sender resolvers registered
     * in this {@link Lamp} instance
     *
     * @return The sender resolvers
     * @see Builder#senderResolver(SenderResolver)
     */
    public @Unmodifiable @NotNull List<SenderResolver<? super A>> senderResolvers() {
        return senderResolvers;
    }

    /**
     * Returns an immutable list of the parameter resolvers registered
     * in this {@link Lamp} instance
     *
     * @return The parameter resolvers
     * @see Builder#parameterValidator(Class, ParameterValidator)
     */
    public @Unmodifiable @NotNull List<ParameterValidator<A, Object>> parameterValidators() {
        return validators;
    }

    /**
     * Returns the dependency that corresponds to the given type, otherwise
     * throws an {@link IllegalStateException}
     *
     * @param type The dependency type
     * @param <T>  The dependency type
     * @return The dependency
     * @throws IllegalStateException if no suitable dependency was found
     */
    @SuppressWarnings("unchecked")
    public <T> @NotNull T dependency(@NotNull Class<T> type) {
        Supplier<T> supplier = (Supplier<T>) dependencies.get(type);
        if (supplier == null)
            throw new IllegalStateException("Cannot find a suitable dependency for type " + type);
        T value = supplier.get();
        if (value == null)
            throw new IllegalStateException("Received a null dependency for type " + type);
        return value;
    }

    /**
     * Validates a parameter by passing it into the registered {@link ParameterValidator}s
     *
     * @param actor     The actor to validate for
     * @param value     The value to validate
     * @param parameter The parameter to validate for
     */
    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    public <T> void validate(A actor, T value, ParameterNode<A, T> parameter) {
        for (ParameterValidator<A, Object> validator : parameterValidators()) {
            validator.validate(actor, value, (ParameterNode<A, Object>) parameter, this);
        }
    }

    /**
     * Returns the {@link AutoCompleter} of this {@link Lamp} instance
     *
     * @return The auto-complete
     */
    public @NotNull AutoCompleter<A> autoCompleter() {
        return autoCompleter;
    }

    /**
     * Handles the given exception in the given context. This will
     * pass the exception to the {@link #exceptionHandler}
     *
     * @param throwable    The exception to handle
     * @param errorContext The context in which the error occurred. For example,
     *                     if the error was because a parameter was invalid, this
     *                     will be a {@link ErrorContext.ParsingParameter} context,
     *                     which contains information about the parameter being parsed.
     * @see Builder#exceptionHandler(CommandExceptionHandler)
     */
    public void handleException(@NotNull Throwable throwable, @NotNull ErrorContext<A> errorContext) {
        notNull(throwable, "throwable");
        try {
            if (throwable instanceof SelfHandledException) {
                //noinspection unchecked
                SelfHandledException<A> she = (SelfHandledException<A>) throwable;
                she.handle(errorContext);
            }
            if (throwable.getClass().isAnnotationPresent(ThrowableFromCommand.class)) {
                exceptionHandler.handleException(throwable, errorContext);
            } else {
                dispatcherSettings.stackTraceSanitizer().sanitize(throwable);
                exceptionHandler.handleException(new CommandInvocationException(throwable), errorContext);
            }
        } catch (Throwable t) {
            throw new IllegalStateException("The CommandExceptionHandler threw an exception", t);
        }
    }

    /**
     * Creates a new {@link CommandPermission} for the given list of annotations. Note
     * that this will never return {@code null}. If no suitable permission factory
     * was found, it will return {@link CommandPermission#alwaysTrue()}.
     *
     * @param annotations Annotations to check for
     * @return The command permission.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NotNull CommandPermission<A> createPermission(AnnotationList annotations) {
        for (CommandPermission.Factory<? super A> permissionFactory : permissionFactories) {
            CommandPermission<A> permission = permissionFactory.create(annotations, (Lamp) this);
            if (permission != null)
                return permission;
        }
        return CommandPermission.alwaysTrue();
    }

    /**
     * Returns the {@link MessageSender} responsible for sending
     * messages using {@link CommandActor#reply(String)}
     *
     * @return The message sender
     */
    public @NotNull MessageSender<? super A, String> messageSender() {
        return messageSender;
    }

    /**
     * Returns the {@link MessageSender} responsible for sending
     * messages using {@link CommandActor#error(String)}
     *
     * @return The error sender
     */
    public @NotNull MessageSender<? super A, String> errorSender() {
        return errorSender;
    }

    /**
     * Returns the {@link DispatcherSettings} that are used by
     * Lamp under the hood to customize the dispatcher settings.
     *
     * @return The dispatcher settings
     * @see DispatcherSettings
     */
    public @NotNull DispatcherSettings<A> dispatcherSettings() {
        return dispatcherSettings;
    }

    /**
     * Accepts the given visitor by calling {@link LampVisitor#visit(Lamp)} on
     * this {@link Lamp} instance
     *
     * @param visitor Visitor to accept
     * @return This Lamp instance
     */
    @Contract("_ -> this")
    public @NotNull Lamp<A> accept(@NotNull LampVisitor<A> visitor) {
        visitor.visit(this);
        return this;
    }

    public static class Builder<A extends CommandActor> {

        private final ParameterTypes.Builder<A> parameterTypes = ParameterTypes.builder();
        private final SuggestionProviders.Builder<A> suggestionProviders = SuggestionProviders.builder();
        private final Hooks.Builder<A> hooks = Hooks.builder();
        private final List<ParameterValidator<A, Object>> validators = new ArrayList<>();
        private final List<ResponseHandler.Factory<? super A>> responseHandlers = new ArrayList<>();
        private final Map<Class<? extends Annotation>, Set<AnnotationReplacer<?>>> annotationReplacers = new LinkedHashMap<>();
        private final List<SenderResolver<? super A>> senderResolvers = new ArrayList<>();
        private final List<CommandCondition<? super A>> conditions = new ArrayList<>();
        private final List<CommandPermission.Factory<A>> permissionFactories = new ArrayList<>();
        private final Map<Class<?>, Supplier<Object>> dependencies = new HashMap<>();
        private DispatcherSettings.Builder<A> dispatcherSettings = DispatcherSettings.builder();
        private MessageSender<? super A, String> messageSender = CommandActor::sendRawMessage;
        private MessageSender<? super A, String> errorSender = CommandActor::sendRawError;
        private CommandExceptionHandler<A> exceptionHandler = new DefaultExceptionHandler<>();
        private ParameterNamingStrategy namingStrategy = ParameterNamingStrategy.lowerCaseWithSpace();

        public Builder() {
            parameterValidator(Number.class, RangeChecker.INSTANCE);
            parameterValidator(String.class, LengthChecker.INSTANCE);
            senderResolver(CommandActorSenderResolver.INSTANCE);
            responseHandler(SupplierResponseHandler.INSTANCE);
            responseHandler(CompletionStageResponseHandler.INSTANCE);
            responseHandler(OptionalResponseHandler.INSTANCE);
            commandCondition(PermissionConditionChecker.INSTANCE);
            accept(KotlinFeatureRegistry.INSTANCE);
        }

        /**
         * Sets the parameter naming strategy
         *
         * @param namingStrategy The parameter naming strategy
         * @return This builder instance
         * @see ParameterNamingStrategy
         */
        public @NotNull Builder<A> parameterNamingStrategy(ParameterNamingStrategy namingStrategy) {
            this.namingStrategy = notNull(namingStrategy, "naming strategy");
            return this;
        }

        /**
         * Returns the {@link ParameterTypes} builder
         *
         * @return The parameter types builder
         * @see ParameterTypes
         */
        public @NotNull ParameterTypes.Builder<A> parameterTypes() {
            return parameterTypes;
        }

        /**
         * Performs the given {@code consumer} on the {@link #parameterTypes()} builder.
         * This allows for easier chaining of the {@link Builder} instance
         *
         * @param consumer The consumer to perform
         * @return This builder instance
         * @see ParameterTypes
         */
        public @NotNull Builder<A> parameterTypes(@NotNull Consumer<ParameterTypes.Builder<A>> consumer) {
            notNull(consumer, "consumer");
            consumer.accept(parameterTypes);
            return this;
        }

        /**
         * Returns the {@link SuggestionProviders} builder
         *
         * @return The suggestion providers builder
         * @see SuggestionProviders
         */
        public @NotNull SuggestionProviders.Builder<A> suggestionProviders() {
            return suggestionProviders;
        }

        /**
         * Performs the given {@code consumer} on the {@link #suggestionProviders()} builder.
         * This allows for easier chaining of the {@link Builder} instance
         *
         * @param consumer The consumer to perform
         * @return This builder instance
         * @see SuggestionProviders
         */
        public @NotNull Builder<A> suggestionProviders(@NotNull Consumer<SuggestionProviders.Builder<A>> consumer) {
            notNull(consumer, "consumer");
            consumer.accept(suggestionProviders);
            return this;
        }

        /**
         * Performs the given {@code consumer} on the {@link #dispatcherSettings()} builder.
         * This allows for easier chaining of the {@link Builder} instance
         *
         * @param consumer The consumer to perform
         * @return This builder instance
         * @see DispatcherSettings
         */
        public @NotNull Builder<A> dispatcherSettings(@NotNull Consumer<DispatcherSettings.Builder<A>> consumer) {
            notNull(consumer, "consumer");
            consumer.accept(dispatcherSettings);
            return this;
        }

        /**
         * Returns the {@link DispatcherSettings} builder
         *
         * @return The dispatcher settings builder
         * @see DispatcherSettings
         */
        public @NotNull DispatcherSettings.Builder<A> dispatcherSettings() {
            return dispatcherSettings;
        }

        /**
         * Sets the {@link DispatcherSettings} that is used by this {@link Lamp}
         * instance
         *
         * @param settings The settings to set
         * @return This builder
         * @see DispatcherSettings
         */
        public @NotNull Builder<A> dispatcherSettings(@NotNull DispatcherSettings<A> settings) {
            notNull(settings, "dispatcher settings");
            dispatcherSettings = settings.toBuilder();
            return this;
        }

        /**
         * Returns the {@link Hooks} builder
         *
         * @return The hooks builder
         * @see Hooks
         */
        public @NotNull Hooks.Builder<A> hooks() {
            return hooks;
        }

        /**
         * Performs the given {@code consumer} on the {@link #hooks()} builder.
         * This allows for easier chaining of the {@link Builder} instance
         *
         * @param consumer The consumer to perform
         * @return This builder instance
         * @see Hooks
         */
        public @NotNull Builder<A> hooks(@NotNull Consumer<Hooks.Builder<A>> consumer) {
            notNull(consumer, "consumer");
            consumer.accept(hooks);
            return this;
        }

        /**
         * Registers an {@link AnnotationReplacer} for a specified annotation type
         *
         * @param annotationType Annotation to replace
         * @param replacer       The annotation replacer
         * @param <T>            The replaced annotation type
         * @return This builder instance
         * @see AnnotationReplacer
         * @see AnnotationList
         */
        public <T extends Annotation> Builder<A> annotationReplacer(@NotNull Class<T> annotationType, @NotNull AnnotationReplacer<T> replacer) {
            notNull(annotationType, "annotation type");
            notNull(replacer, "annotation replacer");
            checkRetention(annotationType);
            annotationReplacers.computeIfAbsent(annotationType, k -> new HashSet<>()).add(replacer);
            return this;
        }

        /**
         * Registers a {@link SenderResolver} which is responsible for parsing the first
         * parameter in the command and testing if it should be assumed the sender type
         *
         * @param resolver The sender resolver
         * @return This builder instance
         * @see SenderResolver
         */
        public Builder<A> senderResolver(@NotNull SenderResolver<? super A> resolver) {
            notNull(resolver, "sender resolver");
            senderResolvers.add(resolver);
            return this;
        }

        /**
         * Registers a {@link ParameterValidator} which validates certain types of parameters
         * that get resolved by {@link ParameterType}s
         *
         * @param type      The parameter type
         * @param validator The validator
         * @param <T>       The parameter type
         * @return This builder instance
         * @see ParameterValidator
         */
        @SuppressWarnings("rawtypes")
        public <T> Builder<A> parameterValidator(Class<T> type, @NotNull ParameterValidator<? super A, T> validator) {
            notNull(type, "type");
            notNull(validator, "parameter validator");
            Class<?> wrapped = wrap(type);
            validators.add((actor, value, parameter, lamp) -> {
                if (!wrapped.isAssignableFrom(wrap(parameter.type())))
                    return;
                //noinspection unchecked
                validator.validate(actor, (T) value, ((ParameterNode) parameter), (Lamp) lamp);
            });
            return this;
        }

        /**
         * Registers a {@link ResponseHandler} that accepts the objects matching a certain
         * type returned by command functions.
         *
         * @param type            The response type
         * @param responseHandler The response handler
         * @param <T>             The response type
         * @return This builder instance
         * @see ResponseHandler
         */
        public <T> Builder<A> responseHandler(Class<T> type, @NotNull ResponseHandler<? super A, T> responseHandler) {
            notNull(type, "type");
            return responseHandler(ResponseHandler.Factory.forType(type, responseHandler));
        }

        /**
         * Registers a {@link ResponseHandler.Factory} that accepts returned by command functions.
         * This factory can access annotations of the type
         *
         * @param responseHandler The response handler
         * @return This builder instance
         * @see ResponseHandler
         */
        public Builder<A> responseHandler(@NotNull ResponseHandler.Factory<? super A> responseHandler) {
            notNull(responseHandler, "response handler");
            responseHandlers.add(responseHandler);
            return this;
        }

        /**
         * Registers a {@link CommandCondition} that is invoked before a command is
         * finally executed
         *
         * @param condition The condition to register
         * @return This builder instance
         * @see CommandCondition
         */
        public Builder<A> commandCondition(@NotNull CommandCondition<? super A> condition) {
            notNull(condition, "command condition");
            conditions.add(condition);
            return this;
        }

        /**
         * Registers a {@link CommandPermission.Factory} that creates {@link CommandPermission}s
         * for commands and parameters
         *
         * @param factory The permission factory
         * @return This builder factory
         * @see CommandPermission.Factory
         * @see CommandPermission
         */
        public Builder<A> permissionFactory(@NotNull CommandPermission.Factory<? super A> factory) {
            notNull(factory, "permission factory");
            //noinspection unchecked
            permissionFactories.add((CommandPermission.Factory<A>) factory);
            return this;
        }

        /**
         * Registers a {@link CommandPermission} function that creates {@link CommandPermission}s
         * for commands and parameters that have the given {@code }
         *
         * @param annotationType    The annotation type
         * @param permissionCreator The permission creator function
         * @return This builder factory
         * @see CommandPermission.Factory
         * @see CommandPermission
         */
        public <T extends Annotation> Builder<A> permissionForAnnotation(
                @NotNull Class<T> annotationType,
                @NotNull Function<@NotNull T, @Nullable CommandPermission<A>> permissionCreator
        ) {
            notNull(annotationType, "annotation type");
            notNull(permissionCreator, "permission creator");
            permissionFactories.add(CommandPermission.Factory.forAnnotation(annotationType, permissionCreator));
            return this;
        }

        /**
         * Sets the registered exception handler that handles all
         * exceptions passed to this builder.
         *
         * @param handler The exception handler
         * @return This builder
         * @see CommandExceptionHandler
         */
        public Builder<A> exceptionHandler(@NotNull CommandExceptionHandler<A> handler) {
            notNull(handler, "exception handler");
            this.exceptionHandler = handler;
            return this;
        }

        /**
         * Registers the given dependency as an object
         *
         * @param dependencyType The dependency class
         * @param dependency     The dependency object
         * @param <T>            The dependency type
         * @return This builder instance
         * @see Dependency
         */
        public <T> Builder<A> dependency(Class<T> dependencyType, @NotNull T dependency) {
            notNull(dependencyType, "dependency type");
            notNull(dependency, "dependency");
            dependencies.put(dependencyType, () -> dependency);
            return this;
        }

        /**
         * Registers the given dependency as a supplier that will provide
         * the values as needed
         *
         * @param dependencyType The dependency class
         * @param dependency     The dependency supplier
         * @param <T>            The dependency type
         * @return This builder instance
         * @see Dependency
         */
        public <T> Builder<A> dependency(Class<T> dependencyType, @NotNull Supplier<T> dependency) {
            notNull(dependencyType, "dependency type");
            notNull(dependency, "dependency");
            //noinspection unchecked
            dependencies.put(dependencyType, (Supplier<Object>) dependency);
            return this;
        }

        /**
         * Registers the default message sender used by {@link CommandActor#reply(String)}
         *
         * @param messageSender The sender to use
         * @return This builder instance
         */
        public Builder<A> defaultMessageSender(@NotNull MessageSender<? super A, String> messageSender) {
            notNull(messageSender, "message sender");
            this.messageSender = messageSender;
            return this;
        }

        /**
         * Registers the default message sender used by {@link CommandActor#error(String)}
         *
         * @param messageSender The sender to use
         * @return This builder instance
         */
        public Builder<A> defaultErrorSender(@NotNull MessageSender<? super A, String> messageSender) {
            notNull(messageSender, "message sender");
            this.errorSender = messageSender;
            return this;
        }

        /**
         * Accepts the given visitor by calling {@link LampBuilderVisitor#visit(Builder)} on
         * this {@link Builder} instance
         *
         * @param visitor Visitor to accept
         * @return This builder instance
         */
        @Contract("_ -> this")
        @SuppressWarnings({"rawtypes", "unchecked"})
        public @NotNull Builder<A> accept(@NotNull LampBuilderVisitor<? super A> visitor) {
            visitor.visit((Builder) this);
            return this;
        }

        /**
         * Constructs a {@link Lamp} from this {@link Builder}. All
         * lists are copied and turned immutable.
         *
         * @return The created {@link Lamp} instance.
         */
        @Contract(pure = true, value = "-> new")
        public Lamp<A> build() {
            return new Lamp<>(this);
        }
    }
}
