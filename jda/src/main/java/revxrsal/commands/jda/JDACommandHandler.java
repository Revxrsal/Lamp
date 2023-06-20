package revxrsal.commands.jda;

import java.util.List;

import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
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
     * Registers a {@link SlashCommandData} using {@link JDA#updateCommands()}
     *
     * @return This command handler
     * @see SlashCommandMapper
     */
    @NotNull JDACommandHandler registerSlashCommands();

    /**
     * Registers a {@link SlashCommandMapper} to this handler
     *
     * @param commandMapper Mapper to register
     * @return This command handler
     * @see SlashCommandMapper
     */
    @NotNull JDACommandHandler registerSlashCommandMapper(@NotNull SlashCommandMapper commandMapper);

    /**
     * Registers a {@link SlashCommandMapper} to this handler
     *
     * @param commandMapper Mapper to register
     * @param priority The parser priority. Zero represents the highest.
     * @return This command handler
     * @see SlashCommandMapper
     */
    @NotNull JDACommandHandler registerSlashCommandMapper(int priority, @NotNull SlashCommandMapper commandMapper);

    /**
     * Returns an unmodifiable view of all the registered slash command parsers
     * in this command handler.
     *
     * @return The registered slash command parsers
     */
    @NotNull @UnmodifiableView List<SlashCommandMapper> getSlashCommandMappers();

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
        return new JDAHandler(jda, prefix);
    }
}
