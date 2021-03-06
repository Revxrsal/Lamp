package revxrsal.commands.bungee.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.exception.ThrowableFromCommand;

/**
 * Thrown when a player-only command is executed by a non-player
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class SenderNotPlayerException extends RuntimeException {

}
