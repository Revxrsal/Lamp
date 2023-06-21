package revxrsal.commands.jda;

import org.jetbrains.annotations.NotNull;

import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.core.adapter.SlashCommandAdapter;

/**
 * Modifies {@link SlashCommandAdapter} based on {@link ExecutableCommand}.
 */
public interface SlashCommandMapper {
    /**
     *  Modifies existing slash command, or subcommand. For example changing description of slash commands.
     *
     * @param slashCommandAdapter Slash command or subcommand that will be modified
     * @param command             Command that 'represents' slash command
     * @see SlashCommandAdapter
     */
    void mapSlashCommand(@NotNull SlashCommandAdapter slashCommandAdapter, @NotNull ExecutableCommand command);
}
