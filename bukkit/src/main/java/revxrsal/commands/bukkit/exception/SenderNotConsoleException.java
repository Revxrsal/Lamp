package revxrsal.commands.bukkit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Thrown when a console-only command is executed by a non-console
 */
@Getter
@AllArgsConstructor
public class SenderNotConsoleException extends RuntimeException {

}
