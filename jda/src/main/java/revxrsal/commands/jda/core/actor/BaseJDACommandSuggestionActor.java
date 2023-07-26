package revxrsal.commands.jda.core.actor;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.jda.actor.SlashCommandSuggestionJDAActor;

public class BaseJDACommandSuggestionActor extends BaseActorJDA implements SlashCommandSuggestionJDAActor {
    public BaseJDACommandSuggestionActor(Event event, CommandHandler handler) {
        super(event, handler);
    }

    @Override
    public @NotNull User getUser() {
        return getSuggestionEvent().getUser();
    }

    @Override
    public @NotNull MessageChannel getChannel() {
        return getSuggestionEvent().getChannel();
    }

    @Override
    public boolean isGuildEvent() {
        return getSuggestionEvent().isGuildCommand();
    }

    @Override
    public @NotNull Guild getGuild() {
        return getSuggestionEvent().getGuild();
    }

    @Override
    public @NotNull Member getMember() {
        return getSuggestionEvent().getMember();
    }
}
