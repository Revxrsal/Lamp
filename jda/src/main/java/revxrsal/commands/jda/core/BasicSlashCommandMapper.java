package revxrsal.commands.jda.core;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.SlashCommandMapper;
import revxrsal.commands.jda.annotation.GuildOnly;
import revxrsal.commands.jda.annotation.GuildPermission;
import revxrsal.commands.jda.core.adapter.SlashCommandAdapter;

public class BasicSlashCommandMapper implements SlashCommandMapper {
    @Override
    public void mapSlashCommand(@NotNull SlashCommandAdapter slashCommandAdapter, @NotNull ExecutableCommand command) {
        if (slashCommandAdapter.isSlashCommand()) {
            SlashCommandData slashCommandData = slashCommandAdapter.getCommandData();
            if (command.hasAnnotation(GuildOnly.class))
                slashCommandData.setGuildOnly(true);
            if (command.hasAnnotation(GuildPermission.class))
                slashCommandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.getAnnotation(GuildPermission.class).value()));
        }
    }
}
