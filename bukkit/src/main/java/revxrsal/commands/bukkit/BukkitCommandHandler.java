package revxrsal.commands.bukkit;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.core.BukkitHandler;

/**
 * Represents Bukkit's command handler implementation
 */
public interface BukkitCommandHandler extends CommandHandler {

    /**
     * Registers commands automatically on Minecraft's 1.13+ command system
     * (so that you would get the colorful command completions!)
     * <p>
     * Note that you should call this method after you've registered
     * all your commands.
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

