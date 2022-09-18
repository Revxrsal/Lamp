package revxrsal.commands.bukkit;

import java.util.Optional;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.core.BukkitHandler;

/**
 * Represents Bukkit's command handler implementation
 */
public interface BukkitCommandHandler extends CommandHandler {

  /**
   * Returns an optional {@link BukkitBrigadier} of this command handler.
   * <p>
   * On versions that do not support Brigadier (i.e. 1.12.2 or earlier), this optional will be
   * empty.
   *
   * @return The Brigadier accessor
   */
  @NotNull Optional<BukkitBrigadier> getBrigadier();

  /**
   * Checks to see if the Brigadier command system is supported by the server.
   *
   * @return true if Brigadier is supported.
   */
  boolean isBrigadierSupported();

  /**
   * Registers commands automatically on Minecraft's 1.13+ command system (so that you would get the
   * colorful command completions!)
   * <p>
   * Note that you should call this method after you've registered all your commands.
   * <p>
   * This is effectively the same as {@code getBrigadier().register()}, and will have no effect when
   * invoked on older versions.
   *
   * @return This command handler
   */
  BukkitCommandHandler registerBrigadier();

  /**
   * Returns the plugin this command handler was registered for.
   *
   * @return The owning plugin
   */
  @NotNull Plugin getPlugin();

  /**
   * Creates a new {@link BukkitCommandHandler} for the specified plugin
   *
   * @param plugin Plugin to create for
   * @return The newly created command handler
   */
  static @NotNull BukkitCommandHandler create(@NotNull Plugin plugin) {
    return new BukkitHandler(plugin);
  }
}

