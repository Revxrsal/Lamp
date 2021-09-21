package revxrsal.commands.jda.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.ThrowableFromCommand;

/**
 * Thrown when a guild-only command is used in a private channel.
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class GuildOnlyCommandException extends RuntimeException {

    /**
     * The command being executed
     */
    private final ExecutableCommand command;

}
