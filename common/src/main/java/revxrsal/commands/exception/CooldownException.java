package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.command.CommandActor;

import java.util.concurrent.TimeUnit;

import static revxrsal.commands.util.Preconditions.notNull;

/**
 * Thrown when the {@link CommandActor} has to wait before executing a
 * command again. This is set by {@link Cooldown}.
 */
public class CooldownException extends RuntimeException {

    /**
     * The time left (in milliseconds)
     */
    private final long timeLeft;

    /**
     * Creates a new {@link CooldownException} with the given timestamp
     * in milliseconds
     *
     * @param timeLeft The time left in milliseconds
     */
    public CooldownException(long timeLeft) {
        this.timeLeft = timeLeft;
    }

    /**
     * Creates a new {@link CooldownException} with the given timestamp
     * in any unit
     *
     * @param timeLeft The time left in the given unit
     */
    public CooldownException(TimeUnit unit, long timeLeft) {
        this.timeLeft = unit.toMillis(timeLeft);
    }

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
