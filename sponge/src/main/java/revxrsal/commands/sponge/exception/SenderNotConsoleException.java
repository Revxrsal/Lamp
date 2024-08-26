package revxrsal.commands.sponge.exception;

import revxrsal.commands.exception.ThrowableFromCommand;

/**
 * Thrown when a console-only command is executed by a non-console
 */
@ThrowableFromCommand
public class SenderNotConsoleException extends RuntimeException {

}
