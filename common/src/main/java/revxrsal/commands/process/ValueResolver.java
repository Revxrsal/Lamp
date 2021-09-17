package revxrsal.commands.process;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.CommandExceptionHandler;

/**
 * A resolver for resolving values that, by default, require data from the arguments
 * to resolve their value.
 * An example context resolver is finding a player from their name.
 *
 * @param <T> The resolved type
 */
public interface ValueResolver<T> {

    /**
     * Resolves the value of this resolver
     *
     * @param arguments The command arguments passed to the command.
     * @param actor     The command actor
     * @param parameter The parameter to resolve
     * @return The resolved value. May or may not be null.
     * @throws Throwable Any exceptions that should be handled by {@link CommandExceptionHandler}
     */
    @Contract(mutates = "param1")
    T resolve(@NotNull ArgumentStack arguments,
              @NotNull CommandActor actor,
              @NotNull CommandParameter parameter,
              @NotNull ExecutableCommand command) throws Throwable;

}
