package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;

/**
 * Represents an exception that is purely used to send messages directly to
 * the sender. Exceptions thrown
 */
public abstract class SendableException extends RuntimeException {

    /**
     * The command actor. In most cases, this can be left null, however
     * in exceptions thrown in certain circumstances it may be impossible
     * to infer the actor, in which case it must be explicitly specified
     */
    private final @Nullable CommandActor actor;

    /**
     * Constructs a new {@link SendableException} with an inferred actor
     *
     * @param message Message to send
     */
    public SendableException(String message) {
        this(null, message);
    }

    /**
     * Constructs a new {@link SendableException} with a specified actor
     *
     * @param actor   Actor to send to
     * @param message Message to send
     */
    public SendableException(@Nullable CommandActor actor, String message) {
        super(message);
        this.actor = actor;
    }

    /**
     * Sends the message to the given actor
     *
     * @param actor Actor to send to
     */
    public abstract void sendTo(@NotNull CommandActor actor);

    /**
     * Sends the message to the actor. It must be explicitly specified
     */
    public void send() {
        if (actor == null) throw new IllegalArgumentException("No actor specified.");
        sendTo(actor);
    }

}
