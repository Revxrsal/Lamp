package revxrsal.commands.sponge;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.sponge.core.SpongeActor;
import revxrsal.commands.sponge.exception.SenderNotConsoleException;
import revxrsal.commands.sponge.exception.SenderNotPlayerException;

/**
 * Represents a Sponge {@link CommandActor} that wraps a {@link CommandSource}
 */
public interface SpongeCommandActor extends CommandActor {

    /**
     * Sends a message to this receiver.
     *
     * <p>If text formatting is not supported in the implementation
     * it will be displayed as plain text.</p>
     *
     * @param message The message
     */
    void reply(@NotNull Text message);

    /**
     * Returns the underlying {@link CommandSource} being wrapped by this
     * actor.
     *
     * @return Velocity's command source.
     */
    @NotNull CommandSource getSource();

    /**
     * Tests whether is this actor a player or not
     *
     * @return Is this a player or not
     */
    boolean isPlayer();

    /**
     * Tests whether is this actor the console or not
     *
     * @return Is this the console or not
     */
    boolean isConsole();

    /**
     * Returns this actor as a {@link Player} if it is a player,
     * otherwise returns {@code null}.
     *
     * @return The sender as a player, or null.
     */
    @Nullable Player getAsPlayer();

    /**
     * Returns this actor as a {@link Player} if it is a player,
     * otherwise throws a {@link SenderNotPlayerException}.
     *
     * @return The actor as a player
     * @throws SenderNotPlayerException if not a player
     */
    @NotNull Player requirePlayer() throws SenderNotPlayerException;

    /**
     * Returns this actor as a {@link ConsoleSource} if it is a player,
     * otherwise throws a {@link SenderNotConsoleException}.
     *
     * @return The actor as console
     * @throws SenderNotConsoleException if not a player
     */
    @NotNull ConsoleSource requireConsole() throws SenderNotConsoleException;

    /**
     * Creates a new {@link SpongeCommandActor} that wraps the given {@link CommandSource}.
     *
     * @param source Command source to wrap
     * @return The wrapping {@link SpongeCommandActor}.
     */
    static @NotNull SpongeCommandActor wrap(@NotNull CommandSource source, @NotNull CommandHandler handler) {
        return new SpongeActor(source, handler);
    }

}
