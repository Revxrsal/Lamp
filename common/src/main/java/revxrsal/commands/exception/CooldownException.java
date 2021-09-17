package revxrsal.commands.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.annotation.Cooldown;

import java.util.concurrent.TimeUnit;

import static revxrsal.commands.util.Preconditions.notNull;

/**
 * Thrown when the {@link CommandActor} has to wait before executing a
 * command again. This is set by {@link Cooldown}.
 */
@AllArgsConstructor
public class CooldownException extends RuntimeException {

    /**
     * The command actor
     */
    @Getter private final CommandActor actor;

    /**
     * The time left (in milliseconds)
     */
    private final long timeLeft;

    /**
     * Returns the time left before being able to execute again
     *
     * @return Time left in milliseconds
     */
    public long getTimeLeftMillis() {
        return timeLeft;
    }

    /**
     * Returns the time left in the given unit
     *
     * @param unit Unit to convert to
     * @return The time left
     */
    public long getTimeLeft(@NotNull TimeUnit unit) {
        notNull(unit, "unit");
        return unit.convert(timeLeft, TimeUnit.MILLISECONDS);
    }
}
