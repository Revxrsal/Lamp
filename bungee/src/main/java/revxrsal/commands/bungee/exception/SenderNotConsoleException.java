package revxrsal.commands.bungee.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import revxrsal.commands.exception.ThrowableFromCommand;

/**
 * Thrown when a console-only command is executed by a non-console
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class SenderNotConsoleException extends RuntimeException {

}
