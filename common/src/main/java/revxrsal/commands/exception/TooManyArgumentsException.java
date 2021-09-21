package revxrsal.commands.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.ExecutableCommand;

/**
 * Thrown when extra arguments are inputted for a command. By default, this
 * exception is not thrown, unless specifend by {@link CommandHandler#failOnTooManyArguments()}.
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class TooManyArgumentsException extends RuntimeException {

    /**
     * The command being executed
     */
    private final @NotNull ExecutableCommand command;

    /**
     * The extra arguments
     */
    private final @NotNull ArgumentStack arguments;
}
