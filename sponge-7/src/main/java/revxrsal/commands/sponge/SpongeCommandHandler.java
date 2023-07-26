package revxrsal.commands.sponge;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.sponge.core.SpongeHandler;

/**
 * Represents Sponge's command handler implementation
 */
public interface SpongeCommandHandler extends CommandHandler {

    /**
     * Returns the plugin this command handler was registered for.
     *
     * @return The owning plugin
     */
    @NotNull Object getPlugin();

    /**
     * Creates a new {@link SpongeCommandHandler} for the specified plugin
     *
     * @param plugin Plugin to create for
     * @return The newly created command handler
     */
    static @NotNull SpongeCommandHandler create(@NotNull Object plugin) {
        return new SpongeHandler(plugin);
    }

}
