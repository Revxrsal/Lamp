package revxrsal.commands.jda.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.ThrowableFromCommand;

/**
 * Thrown when a private-message only command is executed in a guild.
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class PrivateMessageOnlyCommandException extends RuntimeException {

    /**
     * The command being executed
     */
    private final ExecutableCommand command;

}
