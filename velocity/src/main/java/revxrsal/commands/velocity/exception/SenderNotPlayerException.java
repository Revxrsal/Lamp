package revxrsal.commands.velocity.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.ThrowableFromCommand;
import revxrsal.commands.velocity.VelocityCommandActor;

/**
 * Thrown when a player-only command is executed by a non-player
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class SenderNotPlayerException extends RuntimeException {

    /**
     * The command actor that failed to be a player.
     */
    private final @NotNull VelocityCommandActor actor;

}
