package revxrsal.commands;

import dev.demeng.pluginbase.locale.Translator;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Flag;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.annotation.dynamic.AnnotationReplacer;
import revxrsal.commands.annotation.dynamic.Annotations;
import revxrsal.commands.autocomplete.AutoCompleter;
import revxrsal.commands.command.ArgumentParser;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.CommandPath;
import revxrsal.commands.core.reflect.MethodCallerFactory;
import revxrsal.commands.exception.ArgumentParseException;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.exception.TooManyArgumentsException;
import revxrsal.commands.help.CommandHelp;
import revxrsal.commands.help.CommandHelpWriter;
import revxrsal.commands.process.CommandCondition;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ContextResolverFactory;
import revxrsal.commands.process.ParameterValidator;
import revxrsal.commands.process.PermissionReader;
import revxrsal.commands.process.ResponseHandler;
import revxrsal.commands.process.SenderResolver;
import revxrsal.commands.process.ValueResolver;
import revxrsal.commands.process.ValueResolverFactory;

/**
 * The main handler for registering commands, resolvers, interceptors, handlers,
 * tab completions and other stuff.
 */
public interface CommandHandler {

    /**
     * Registers the specified command from an instance. This will automatically
     * set all {@link Dependency}-annotated fields with their values.
     *
     * @param commands The commands object instances. Can be a class if methods are static.
     * @return This command handler
     */
    @NotNull CommandHandler register(@NotNull Object... commands);

    /**
     * Gets the current, default locale used by this handler
     *
     * @return The default locale
     */
    @NotNull Locale getLocale();

    /**
     * Sets the locale of this handler.
     *
     * @param locale The locale of this handler
     */
    void setLocale(@NotNull Locale locale);

    /**
     * Returns the translator of this command handler
     *
     * @return The message translator
     */
    @NotNull Translator getTranslator();

    /**
     * Sets the {@link MethodCallerFactory} responsible for generating access
     * to reflection methods.
     * <p>
     * This method allows using alternative strategy for accessing methods
     * reflectively.
     *
     * @param factory Factory to set
     * @return This command handler
     * @see MethodCallerFactory
     */
    @NotNull CommandHandler setMethodCallerFactory(@NotNull MethodCallerFactory factory);

    /**
     * Returns the {@link ArgumentParser} responsible for controlling
     * the logic of parsing strings.
     *
     * @return The argument parser.
     */
    @NotNull ArgumentParser getArgumentParser();

    /**
     * Parses the string and returns an {@link ArgumentStack} for it. This
     * method behaves exactly like {@link #parseArguments(String[])}, however
     * it returns a singleton list of an empty string when the text is empty,
     * suitable for auto-completion.
     *
     * @param arguments Strings to parse. These will get joined
     *                  to a string separated by spaces.
     * @return The argument stack.
     */
    ArgumentStack parseArgumentsForCompletion(String... arguments) throws ArgumentParseException;

    /**
     * Parses the string array and returns an {@link ArgumentStack} for it.
     *
     * @param arguments String array to parse. This will be joined as a single string
     *                  with spaces.
     * @return The argument stack.
     * @see ArgumentParser#parse(String).
     */
    ArgumentStack parseArguments(String... arguments) throws ArgumentParseException;

    /**
     * Sets the {@link ArgumentParser} responsible for controlling
     * the logic of parsing strings.
     *
     * @param parser The argument parser to use.
     */
    CommandHandler setArgumentParser(@NotNull ArgumentParser parser);

    /**
     * Sets the {@link CommandExceptionHandler} to use for handling any exceptions
     * that are thrown from the command.
     * <p>
     * If not set, a default one will be used.
     *
     * @param handler The exception handler
     * @return This command handler
     * @see CommandExceptionHandler#handleException(Throwable, CommandActor)
     */
    @NotNull CommandHandler setExceptionHandler(@NotNull CommandExceptionHandler handler);

    /**
     * Convenient method to register exception handlers. These will have higher priority
     * over {@link CommandExceptionHandler}.
     *
     * @param exceptionType The exception class
     * @param handler       The exception handler
     * @return This command handler
     * @see CommandExceptionHandler#handleException(Throwable, CommandActor)
     */
    @NotNull <T extends Throwable> CommandHandler registerExceptionHandler(@NotNull Class<T> exceptionType,
                                                                           @NotNull BiConsumer<CommandActor, T> handler);

    /**
     * Sets the prefix that all parameters annotated with {@link Switch} will
     * be checked against. If not set, <blockquote>-</blockquote> will be used
     *
     * @param prefix New prefix to set
     * @return This command handler
     * @throws IllegalArgumentException if the prefix is empty
     */
    @NotNull CommandHandler setSwitchPrefix(@NotNull String prefix);

    /**
     * Sets the prefix that all parameters annotated with {@link Flag} will
     * be checked against. If not set, <blockquote>-</blockquote> will be used
     *
     * @param prefix New prefix to set
     * @return This command handler
     * @throws IllegalArgumentException if the prefix is empty
     */
    @NotNull CommandHandler setFlagPrefix(@NotNull String prefix);

    /**
     * Returns the prefix that should preced all messages sent
     * with {@link CommandActor#reply(String)} and {@link CommandActor#error(String)}.
     * <p>
     * Note that the prefix will <em>NOT</em> be followed by spaces, it is your
     * responsibility to include it in the prefix you set.
     *
     * @param prefix Message prefix
     * @return This command handler
     */
    @NotNull CommandHandler setMessagePrefix(@NotNull String prefix);

    /**
     * Sets the {@link CommandHelpWriter} responsible for generating help pages
     *
     * @param <T>        The help entry type.
     * @param helpWriter Help writer to use
     * @return This command handler
     * @see CommandHelpWriter
     * @see CommandHelp
     */
    @NotNull <T> CommandHandler setHelpWriter(@NotNull CommandHelpWriter<T> helpWriter);

    /**
     * Disables stacktrace sanitization.
     * <p>
     * By default, printed stack-trace is sanitized and stripped from internal,
     * extra trace elements. This helps to keep the trace clean and readable,
     * and removes away unnecessary paths. When disabled, full stacktrace
     * will be printed.
     *
     * @return This command handler
     */
    @NotNull CommandHandler disableStackTraceSanitizing();

    /**
     * Sets the command to fail when too many arguments are specified
     * in the command.
     *
     * @return This command handler
     * @see TooManyArgumentsException
     */
    @NotNull CommandHandler failOnTooManyArguments();

    /**
     * Registers the given sender resolver, which resolves parameters at index 0
     * that may be potentially a custom sender implementation.
     * <p>
     * See {@link SenderResolver} for more information.
     *
     * @param resolver Resolver to register
     * @return This command handler
     * @see SenderResolver
     */
    @NotNull CommandHandler registerSenderResolver(@NotNull SenderResolver resolver);

    /**
     * Registers the given permission reader, which allows registering
     * custom {@link CommandPermission} implementations with annotations.
     *
     * @param reader Permission reader to register
     * @return This command handler
     * @see PermissionReader
     */
    @NotNull CommandHandler registerPermissionReader(@NotNull PermissionReader reader);

    /**
     * Registers a parameter resolver that gets its value from the command arguments.
     * <p>
     * See {@link ValueResolver} for more information
     *
     * @param type     The parameter type to resolve
     * @param resolver The resolver
     * @return This command handler
     * @see ValueResolver
     */
    @NotNull <T> CommandHandler registerValueResolver(@NotNull Class<T> type, @NotNull ValueResolver<T> resolver);

    /**
     * Registers a parameter resolver that gets its value from the command arguments.
     * <p>
     * See {@link ValueResolver} for more information
     *
     * @param priority The resolver priority. Zero represents the highest.
     * @param type     The parameter type to resolve
     * @param resolver The resolver
     * @return This command handler
     * @see ValueResolver
     */
    @NotNull <T> CommandHandler registerValueResolver(int priority, @NotNull Class<T> type, @NotNull ValueResolver<T> resolver);

    /**
     * Registers a parameter resolver that gets its value from the command context.
     * <p>
     * See {@link ContextResolver} for more information
     *
     * @param type     The parameter type to resolve
     * @param resolver The resolver
     * @return This command handler
     * @see ContextResolver
     */
    @NotNull <T> CommandHandler registerContextResolver(@NotNull Class<T> type, @NotNull ContextResolver<T> resolver);

    /**
     * Registers a parameter resolver that gets its value from the command context.
     * <p>
     * See {@link ContextResolver} for more information
     *
     * @param priority The resolver priority. Zero represents the highest.
     * @param type     The parameter type to resolve
     * @param resolver The resolver
     * @return This command handler
     * @see ContextResolver
     */
    @NotNull <T> CommandHandler registerContextResolver(int priority, @NotNull Class<T> type, @NotNull ContextResolver<T> resolver);

    /**
     * Registers a parameter type to always be a static value. This is useful
     * for registering singletons as parameters.
     * <p>
     * This is equivalent to calling {@code registerContextResolver(type, ContextResolver.of(type))}
     * <p>
     * See {@link ContextResolver} for more information
     *
     * @param type  The parameter type to resolve
     * @param value The value to retrun
     * @return This command handler
     * @see ContextResolver
     */
    @NotNull <T> CommandHandler registerContextValue(@NotNull Class<T> type, @Nullable T value);

    /**
     * Registers a parameter type to always be a static value. This is useful
     * for registering singletons as parameters.
     * <p>
     * This is equivalent to calling {@code registerContextResolver(priority, type, ContextResolver.of(value))}
     * <p>
     * See {@link ContextResolver} for more information
     *
     * @param priority The resolver priority. Zero represents the highest.
     * @param type     The parameter type to resolve
     * @param value    The value to retrun
     * @return This command handler
     * @see ContextResolver
     */
    @NotNull <T> CommandHandler registerContextValue(int priority, @NotNull Class<T> type, @NotNull T value);

    /**
     * Registers a {@link ValueResolverFactory} to this handler
     *
     * @param factory Factory to register
     * @return This command handler
     * @see ValueResolverFactory
     * @see #registerContextResolverFactory(ContextResolverFactory)
     */
    @NotNull CommandHandler registerValueResolverFactory(@NotNull ValueResolverFactory factory);

    /**
     * Registers a {@link ValueResolverFactory} to this handler
     *
     * @param priority The resolver priority. Zero represents the highest.
     * @param factory  Factory to register
     * @return This command handler
     * @see ValueResolverFactory
     * @see #registerContextResolverFactory(ContextResolverFactory)
     */
    @NotNull CommandHandler registerValueResolverFactory(int priority, @NotNull ValueResolverFactory factory);

    /**
     * Registers a {@link ContextResolverFactory} to this handler
     *
     * @param factory Factory to register
     * @return This command handler
     * @see ContextResolverFactory
     * @see #registerValueResolverFactory(ValueResolverFactory)
     */
    @NotNull CommandHandler registerContextResolverFactory(@NotNull ContextResolverFactory factory);

    /**
     * Registers a {@link ContextResolverFactory} to this handler
     *
     * @param priority The resolver priority. Zero represents the highest.
     * @param factory  Factory to register
     * @return This command handler
     * @see ContextResolverFactory
     * @see #registerValueResolverFactory(ValueResolverFactory)
     */
    @NotNull CommandHandler registerContextResolverFactory(int priority, @NotNull ContextResolverFactory factory);

    /**
     * Registers the specified condition in which all commands will be
     * validated with.
     *
     * @param condition Condition to register
     * @return This command handler
     */
    @NotNull CommandHandler registerCondition(@NotNull CommandCondition condition);

    /**
     * Registers a dependency for dependency injection.
     * <p>
     * Any fields in the command class or instance with the {@link Dependency} annotation
     * will have their value set from this supplier.
     *
     * @param <T>      The dependency type
     * @param type     The dependency class type. This <i>must</i> match
     *                 the field type.
     * @param supplier The dependency supplier
     * @return This command handler
     * @see #registerDependency(Class, Object)
     */
    @NotNull <T> CommandHandler registerDependency(@NotNull Class<T> type, @NotNull Supplier<T> supplier);

    /**
     * Registers a (static) dependency for dependency injection.
     * <p>
     * Any fields in the command class or instance with the {@link Dependency} annotation
     * will have their value set to this value.
     *
     * @param <T>   The dependency type
     * @param type  The dependency class type. This <i>must</i> match
     *              the field type
     * @param value The dependency value
     * @return This command handler
     * @see #registerDependency(Class, Supplier)
     */
    @NotNull <T> CommandHandler registerDependency(@NotNull Class<T> type, T value);

    /**
     * Registers a {@link ParameterValidator} for the specified parameter type. Parameter
     * validators can access all information about a parameter, including the name and annotations.
     *
     * @param <T>       The parameter type
     * @param type      The parameter type
     * @param validator The validator for this parameter
     * @return This command handler
     */
    @NotNull <T> CommandHandler registerParameterValidator(@NotNull Class<T> type, @NotNull ParameterValidator<T> validator);

    /**
     * Registers a response handler for the specified response type. Response handlers
     * do post-handling with results returned from command methods.
     * <p>
     * Note that response handlers are captured by {@link ExecutableCommand}s when they are
     * registered, so they should be registered <i>before</i> the command itself is
     * registered.
     *
     * @param <T>          The response type
     * @param responseType The response class
     * @param handler      The response handler implementation
     * @return This command handler
     */
    @NotNull <T> CommandHandler registerResponseHandler(@NotNull Class<T> responseType, @NotNull ResponseHandler<T> handler);

    /**
     * Registers an annotation replacer. Annotation replacers allow to
     * dynamically generate annotations at runtime using APIs
     * such as {@link Annotations}.
     */
    @NotNull <T extends Annotation> CommandHandler registerAnnotationReplacer(@NotNull Class<T> annotationType,
                                                                              @NotNull AnnotationReplacer<T> replacer);

    /**
     * Accepts the given {@link CommandHandlerVisitor} and allows it to visit
     * this command handler.
     *
     * @param visitor Visitor to accept
     * @return This command handler
     * @see CommandHandlerVisitor
     */
    @NotNull CommandHandler accept(@NotNull CommandHandlerVisitor visitor);

    /**
     * Returns the auto-completion handler of this command handler
     *
     * @return The auto-completion handler
     */
    @NotNull AutoCompleter getAutoCompleter();

    /**
     * Returns the given {@link ExecutableCommand} that matches the given path.
     * This can return null if no command exists at such a path.
     * <p>
     * Note that {@link CommandPath}s are, by default, case-insensitive.
     *
     * @param path Path to look for
     * @return The command at the given path
     */
    ExecutableCommand getCommand(@NotNull CommandPath path);

    /**
     * Returns the given {@link CommandCategory} that matches the given path.
     * This can return null if no category exists at such a path.
     * <p>
     * Note that {@link CommandPath}s are, by default, case-insensitive.
     *
     * @param path Path to look for
     * @return The category at the given path
     */
    CommandCategory getCategory(@NotNull CommandPath path);

    /**
     * Returns an unmodifiable view of all the registered commands
     * in this command handler.
     *
     * @return The registered commands
     */
    @NotNull @UnmodifiableView Map<CommandPath, ExecutableCommand> getCommands();

    /**
     * Returns an unmodifiable view of all the registered categories
     * in this command handler.
     *
     * @return The registered categories
     */
    @NotNull @UnmodifiableView Map<CommandPath, CommandCategory> getCategories();

    /**
     * Returns the command exception handler currently used by this command handler
     *
     * @return The command exception handler
     */
    @NotNull CommandExceptionHandler getExceptionHandler();

    /**
     * Returns the {@link MethodCallerFactory} responsible for generating reflective
     * calls.
     *
     * @return The method caller factory
     */
    @NotNull MethodCallerFactory getMethodCallerFactory();

    /**
     * Returns the {@link CommandHelpWriter} of this command handler. This can
     * be null if no writer is registered.
     *
     * @param <T> Command help entries type
     * @return The help writer of this handler
     */
    <T> CommandHelpWriter<T> getHelpWriter();

    /**
     * Unregisters the given path and all the sub-paths that belong to
     * it
     *
     * @param path Path to unregister
     * @return True if one or more elements were removed by this
     * call.
     */
    boolean unregister(@NotNull CommandPath path);

    /**
     * Unregisters the given path and all the sub-paths that belong to
     * it
     *
     * @param commandPath Path to unregister
     * @return True if one or more elements were removed by this
     * call.
     */
    boolean unregister(@NotNull String commandPath);

    /**
     * Unregisters all the command instances in this command handler.
     */
    void unregisterAllCommands();

    /**
     * Returns all the root commands or categories paths in
     * this command handler.
     *
     * @return All root command paths
     */
    @NotNull Set<CommandPath> getRootPaths();

    /**
     * Returns the prefix that comes before all {@link Switch} parameters
     * when they are fetched from the command.
     *
     * @return The switch prefix
     */
    @NotNull String getSwitchPrefix();

    /**
     * Returns the prefix that comes before all {@link Flag} parameters
     * when they are fetched from the command.
     *
     * @return The switch prefix
     */
    @NotNull String getFlagPrefix();

    /**
     * Returns the prefix that preceds all messages sent by {@link CommandActor#reply(String)}
     * and {@link CommandActor#error(String)}.
     * <p>
     * Set with {@link CommandHandler#setMessagePrefix(String)}
     *
     * @return The message prefix
     */
    @NotNull String getMessagePrefix();

    /**
     * Returns the dependency registered for the given type
     *
     * @param dependencyType Dependency type to look for
     * @param <T>            Dependency type
     * @return The dependency, or null if not found.
     */
    <T> Supplier<T> getDependency(@NotNull Class<T> dependencyType);

    /**
     * Returns the dependency registered for the given type, otherwise
     * returns the given {@code def} value.
     *
     * @param dependencyType Dependency type to look for
     * @param def            Default value if no dependency is registered for
     *                       the given type.
     * @param <T>            Dependency type
     * @return The dependency, or null if not found.
     */
    <T> Supplier<T> getDependency(@NotNull Class<T> dependencyType, Supplier<T> def);

    /**
     * Evaluates the command from the given arguments
     *
     * @param actor     Actor to execute as
     * @param arguments Arguments to invoke the command with
     * @return The result returned from invoking the command method. The
     * optional value may be null if an exception was thrown.
     */
    <T> @NotNull Optional<@Nullable T> dispatch(@NotNull CommandActor actor, @NotNull ArgumentStack arguments);

    /**
     * Evaluates the command from the given input
     *
     * @param actor        Actor to execute as
     * @param commandInput Input to invoke with
     */
    <T> @NotNull Optional<@Nullable T> dispatch(@NotNull CommandActor actor, @NotNull String commandInput);

}
