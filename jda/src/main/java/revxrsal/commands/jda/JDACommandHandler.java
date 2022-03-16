package revxrsal.commands.jda;

import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.jda.core.JDAHandler;

/**
 * Represents JDA's command handler implementation.
 */
public interface JDACommandHandler extends CommandHandler {

    /**
     * Returns the JDA instance maintained by this command handler.
     *
     * @return The JDA instance
     */
    @NotNull JDA getJDA();

    /**
     * Creates a new {@link JDACommandHandler} for the given JDA instance.
     * <p>
     * This will automatically create slash commands.
     *
     * @param jda JDA to create for
     * @return The JDA command handler
     */
    static @NotNull JDACommandHandler create(@NotNull JDA jda) {
        return create(jda, "/");
    }

    /**
     * Creates a new {@link JDACommandHandler} for the given JDA instance,
     * and listens to the given prefix.
     *
     * @param jda    JDA to create for
     * @param prefix The command prefix
     * @return The JDA command handler
     */
    static @NotNull JDACommandHandler create(@NotNull JDA jda, String prefix) {
        return new JDAHandler(jda, prefix, false);
    }
}
