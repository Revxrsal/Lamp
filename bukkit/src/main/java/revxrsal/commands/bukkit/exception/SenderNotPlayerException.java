package revxrsal.commands.bukkit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import revxrsal.commands.exception.ThrowableFromCommand;

/**
 * Thrown when a player-only command is executed by a non-player
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class SenderNotPlayerException extends RuntimeException {

}
