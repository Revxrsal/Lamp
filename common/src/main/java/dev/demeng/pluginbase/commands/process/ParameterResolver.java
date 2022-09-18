package dev.demeng.pluginbase.commands.process;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.command.ArgumentStack;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.process.ContextResolver.ContextResolverContext;
import dev.demeng.pluginbase.commands.process.ValueResolver.ValueResolverContext;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Represents a resolver for a {@link CommandParameter}. Instances of this resolver can be fetched
 * from {@link CommandParameter#getResolver()}.
 *
 * @param <T> The type of the resolved argument
 */
public interface ParameterResolver<T> {

  /**
   * Returns whether this resolver mutates the given arguments when it resolves its value
   *
   * @return If this resolver mutates the {@link ArgumentStack}.
   */
  boolean mutatesArguments();

  /**
   * Resolves the value of the parameter from the given context.
   *
   * @param context The parameter resolver context.
   * @return The resolved value.
   */
  @Nullable
  T resolve(@NotNull ParameterResolverContext context);

  /**
   * Represents the resolving context of a {@link CommandParameter}. This contains all the relevant
   * information about the resolving context.
   *
   * @see ValueResolverContext
   * @see ContextResolverContext
   */
  interface ParameterResolverContext {

    /**
     * Returns the exact input of the actor. This collection represents the tokenized list from the
     * input, without it being modified by resolvers.
     *
     * @return The actor's input to the command.
     */
    @NotNull @Unmodifiable List<String> input();

    /**
     * Returns the command actor that executed this command.
     *
     * @param <A> The actor type.
     * @return The command actor
     */
    @NotNull <A extends CommandActor> A actor();

    /**
     * Returns the current parameter being resolved
     *
     * @return The parameter being resolved
     */
    @NotNull CommandParameter parameter();

    /**
     * Returns the command being executed
     *
     * @return The command being executed
     */
    @NotNull ExecutableCommand command();

    /**
     * Returns the owning {@link CommandHandler} of this resolver.
     *
     * @return The command handler
     */
    @NotNull CommandHandler commandHandler();

    /**
     * Returns the last resolved value of the given parameter type. If no parameter matches the
     * given type, or if the parameter was not resolved yet, this will throw an
     * {@link IllegalStateException}.
     *
     * @param type The parameter type to fetch for.
     * @param <T>  The resolved type
     * @return The last resolved value matching the given type.
     * @throws IllegalStateException If no parameter of this type was resolved.
     */
    <T> @NotNull T getResolvedArgument(@NotNull Class<T> type);

    /**
     * Returns the last resolved value of the given parameter type. If no parameter matches the
     * given type, or if the parameter was not resolved yet, this will throw an
     * {@link IllegalStateException}.
     *
     * @param parameter The parameter to fetch for.
     * @param <T>       The resolved type
     * @return The last resolved value matching the given type.
     * @throws IllegalStateException If no parameter of this type was resolved.
     */
    <T> @NotNull T getResolvedParameter(@NotNull CommandParameter parameter);

  }

}
