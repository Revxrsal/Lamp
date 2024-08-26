package revxrsal.commands.jda.exception;

import revxrsal.commands.exception.ThrowableFromCommand;

/**
 * Thrown when a guild-only command is used in a private channel.
 */
@ThrowableFromCommand
public class GuildOnlyCommandException extends RuntimeException {
}
