package revxrsal.commands.jda;

import org.jetbrains.annotations.NotNull;

import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.core.adapter.SlashCommandAdapter;

/**
 * Maps {@link ExecutableCommand} into {@link }
 */
public interface SlashCommandMapper {
    void mapSlashCommand(@NotNull SlashCommandAdapter slashCommandAdapter, @NotNull ExecutableCommand command);
}
