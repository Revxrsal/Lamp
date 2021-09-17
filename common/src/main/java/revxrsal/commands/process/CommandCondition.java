package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.CommandExceptionHandler;

import java.util.List;

/**
 * Represents a condition that must be met in order for the command
 * invocation to continue.
 * <p>
 * These conditions can test against custom annotations in {@link ExecutableCommand}s,
 * and hence perform external checks for reducing boilerplate
 */
public interface CommandCondition {

    /**
     * Evaluates the condition.
     * <p>
     * Ideally, this should throw any exceptions if the condition fails, and lets
     * them get handled by the {@link CommandExceptionHandler}.
     *
     * @param actor     The command actor
     * @param command   The invoked command
     * @param arguments An immutable view of command arguments
     */
    void test(@NotNull CommandActor actor,
                 @NotNull ExecutableCommand command,
                 @NotNull @Unmodifiable List<String> arguments);

}
