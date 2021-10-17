package revxrsal.commands.velocity.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.ThrowableFromCommand;
import revxrsal.commands.velocity.VelocityCommandActor;

/**
 * Thrown when a console-only command is executed by a non-console
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class SenderNotConsoleException extends RuntimeException {

    /**
     * The command actor that failed to be a console.
     */
    private final @NotNull VelocityCommandActor actor;

}
