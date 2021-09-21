package revxrsal.commands.bukkit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Thrown when a player-only command is executed by a non-player
 */
@Getter
@AllArgsConstructor
public class SenderNotPlayerException extends RuntimeException {

}
