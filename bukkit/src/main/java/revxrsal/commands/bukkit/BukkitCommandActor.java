package revxrsal.commands.bukkit;

import dev.demeng.pluginbase.lib.adventure.audience.Audience;
import dev.demeng.pluginbase.lib.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.core.BukkitActor;
import revxrsal.commands.bukkit.exception.SenderNotConsoleException;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;
import revxrsal.commands.command.CommandActor;

/**
 * Represents a Bukkit {@link CommandActor} that wraps {@link CommandSender}
 */
public interface BukkitCommandActor extends CommandActor {

  /**
   * Returns the underlying {@link CommandSender} of this actor
   *
   * @return The sender
   */
  @NotNull CommandSender getSender();

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
   * Returns this actor as a {@link Player} if it is a player, otherwise returns {@code null}.
   *
   * @return The sender as a player, or null.
   */
  @Nullable Player getAsPlayer();

  /**
   * Returns this actor as a {@link Player} if it is a player, otherwise throws a
   * {@link SenderNotPlayerException}.
   *
   * @return The actor as a player
   * @throws SenderNotPlayerException if not a player
   */
  @NotNull Player requirePlayer() throws SenderNotPlayerException;

  /**
   * Returns this actor as a {@link ConsoleCommandSender} if it is a player, otherwise throws a
   * {@link SenderNotConsoleException}.
   *
   * @return The actor as console
   * @throws SenderNotConsoleException if not a player
   */
  @NotNull ConsoleCommandSender requireConsole() throws SenderNotConsoleException;

  /**
   * Returns the {@link Audience} of this sender.
   *
   * @return The audience of this sender
   */
  @NotNull Audience audience();

  /**
   * Sends the given component to this actor.
   *
   * @param component Component to send
   */
  void reply(@NotNull ComponentLike component);

  /**
   * Returns the command handler that constructed this actor
   *
   * @return The command handler
   */
  @Override
  BukkitCommandHandler getCommandHandler();

  /**
   * Creates a new {@link BukkitCommandActor} that wraps the given {@link CommandSender}.
   *
   * @param sender Command sender to wrap
   * @return The wrapping {@link BukkitCommandActor}.
   */
  static @NotNull BukkitCommandActor wrap(@NotNull CommandSender sender,
      @NotNull CommandHandler handler) {
    return new BukkitActor(sender, handler);
  }
}
