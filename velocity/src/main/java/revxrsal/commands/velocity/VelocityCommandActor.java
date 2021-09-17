package revxrsal.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.velocity.exception.SenderNotConsoleException;
import revxrsal.commands.velocity.exception.SenderNotPlayerException;

/**
 * Represents a Velocity {@link CommandActor} that wraps a {@link CommandSource}
 */
public interface VelocityCommandActor extends CommandActor {

    /**
     * Returns the underlying {@link CommandSource} being wrapped by this
     * actor.
     *
     * @return Velocity's command source.
     */
    @NotNull CommandSource getSource();

    /**
     * Returns the {@link ProxyServer} of this actor
     *
     * @return The proxy server
     */
    @NotNull ProxyServer getServer();

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
     * Returns this actor as a {@link ConsoleCommandSource} if it is a player,
     * otherwise throws a {@link SenderNotConsoleException}.
     *
     * @return The actor as console
     * @throws SenderNotConsoleException if not a player
     */
    @NotNull ConsoleCommandSource requireConsole() throws SenderNotConsoleException;

}
