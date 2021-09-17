package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;

/**
 * Represents a resolver for a {@link CommandParameter}. Instances of this
 * resolver can be fetched from {@link CommandParameter#getResolver()}.
 *
 * @param <T> The type of the resolved argument
 */
public interface ParameterResolver<T> {

    /**
     * Returns whether this resolver mutates the given arguments when it
     * resolves its value
     *
     * @return If this resolver mutates the {@link ArgumentStack}.
     */
    boolean mutatesArguments();

    /**
     * Resolves the value of the parameter from the given context.
     *
     * @param actor     Command actor to resolve for
     * @param parameter Parameter to resolve
     * @param command   The command being executed
     * @param arguments The specified arguments
     * @return The resolved value.
     */
    @Nullable
    T resolve(@NotNull CommandActor actor,
              @NotNull CommandParameter parameter,
              @NotNull ExecutableCommand command,
              @NotNull ArgumentStack arguments);

}
