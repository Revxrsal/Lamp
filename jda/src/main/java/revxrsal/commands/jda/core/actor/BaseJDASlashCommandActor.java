package revxrsal.commands.jda.core.actor;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.jda.actor.SlashCommandJDAActor;

public class BaseJDASlashCommandActor extends BaseActorJDA implements SlashCommandJDAActor {
    public BaseJDASlashCommandActor(SlashCommandInteractionEvent event, CommandHandler handler) {
        super(event, handler);
    }

    @Override
    public void reply(@NotNull String message) {
        getSlashEvent().reply(getCommandHandler().getMessagePrefix() + message).queue();
    }

    @Override
    public void error(@NotNull String message) {
        getSlashEvent().reply(getCommandHandler().getMessagePrefix() + message).queue();
    }

    @Override
    public @NotNull User getUser() {
        return getSlashEvent().getUser();
    }

    @Override
    public @NotNull MessageChannel getChannel() {
        return getSlashEvent().getChannel();
    }

    @Override
    public boolean isGuildEvent() {
        return getSlashEvent().isFromGuild();
    }

    @Override
    public @NotNull Guild getGuild() {
        return getSlashEvent().getGuild();
    }

    @Override
    public @NotNull Member getMember() {
        return getSlashEvent().getMember();
    }
}
