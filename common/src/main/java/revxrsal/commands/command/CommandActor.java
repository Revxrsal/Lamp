package revxrsal.commands.command;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a command sender, responsible for performing a command-related
 * action.
 */
public interface CommandActor {

    /**
     * Returns the name of this actor. Varies depending on the
     * platform.
     *
     * @return The actor name
     */
    @NotNull String getName();

    /**
     * Returns the unique UID of this subject. Varies depending
     * on the platform.
     * <p>
     * Although some platforms explicitly have their underlying senders
     * have UUIDs, some platforms may have to generate this UUID based on other available
     * data.
     *
     * @return The UUID of this subject.
     */
    @NotNull UUID getUniqueId();

    /**
     * Replies to the sender with the specified message.
     * <p>
     * Varies depending on the platform.
     *
     * @param message Message to reply with.
     */
    void reply(@NotNull String message);

    /**
     * Replies to the sender with the specified message, and marks it as
     * an error depending on the platform.
     * <p>
     * Note that, in certain platforms where no "error" mode is available,
     * this may effectively be equivilent to calling {@link #reply(String)}.
     * <p>
     * This method should not throw any exceptions.
     *
     * @param message Message to reply with
     */
    void error(@NotNull String message);

    /**
     * Returns this actor as the specified type. This is effectively
     * casting this actor to the given type.
     *
     * @param type Type to cast to
     * @param <T>  The actor type
     * @return This actor but casted.
     */
    default <T extends CommandActor> T as(@NotNull Class<T> type) {return type.cast(this);}

}
