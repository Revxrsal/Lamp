package revxrsal.commands.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bungee.core.BungeeActor;
import revxrsal.commands.bungee.exception.SenderNotPlayerException;
import revxrsal.commands.command.CommandActor;

/**
 * Represents a Bungee {@link CommandActor} that wraps {@link CommandSender}
 */
public interface BungeeCommandActor extends CommandActor {

    /**
     * The underlying {@link CommandSender} for this subject
     *
     * @return The underlying sender
     */
    CommandSender getSender();

    /**
     * Returns whether is this sender a player or not
     *
     * @return Whether is that sender a player or not
     */
    boolean isPlayer();

    /**
     * Returns this sender as a {@link ProxiedPlayer}.
     *
     * @return This sender as a player. Returns null if not a player.
     */
    @Nullable ProxiedPlayer asPlayer();

    /**
     * Requires this sender to be player, otherwise throws {@link SenderNotPlayerException}.
     *
     * @return The sender as the player
     * @throws SenderNotPlayerException if the sender is not a player (i.e console)
     */
    @NotNull ProxiedPlayer requirePlayer() throws SenderNotPlayerException;

    /**
     * Creates a new {@link BungeeCommandActor} that wraps the given {@link CommandSender}.
     *
     * @param sender Command sender to wrap
     * @return The wrapping {@link BungeeCommandActor}.
     */
    static @NotNull BungeeCommandActor wrap(@NotNull CommandSender sender, @NotNull CommandHandler handler) {
        return new BungeeActor(sender, handler);
    }

}
