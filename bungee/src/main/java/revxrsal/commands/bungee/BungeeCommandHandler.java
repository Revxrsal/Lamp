package revxrsal.commands.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bungee.core.BungeeHandler;

/**
 * Represents Bungee's command handler implementation
 */
public interface BungeeCommandHandler extends CommandHandler {

    /**
     * Returns the plugin this command handler was registered for.
     *
     * @return The owning plugin
     */
    @NotNull Plugin getPlugin();

    /**
     * Creates a new {@link CommandHandler} for the specified plugin
     *
     * @param plugin Plugin to create for
     * @return The newly created command handler
     */
    static @NotNull BungeeCommandHandler create(@NotNull Plugin plugin) {
        return new BungeeHandler(plugin);
    }

}
