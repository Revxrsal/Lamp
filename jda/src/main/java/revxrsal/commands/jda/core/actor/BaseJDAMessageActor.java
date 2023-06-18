package revxrsal.commands.jda.core.actor;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.jda.actor.MessageJDAActor;

public class BaseJDAMessageActor extends BaseActorJDA implements MessageJDAActor {
    public BaseJDAMessageActor(MessageReceivedEvent event, CommandHandler handler) {
        super(event, handler);
    }

    @Override
    public @NotNull User getUser() {
        return getEvent().getAuthor();
    }

    @Override
    public @NotNull MessageChannel getChannel() {
        return getEvent().getChannel();
    }

    @Override
    public boolean isGuildEvent() {
        return getEvent().isFromGuild();
    }

    @Override
    public @NotNull Guild getGuild() {
        return getEvent().getGuild();
    }

    @Override
    public @NotNull Member getMember() {
        return getEvent().getMember();
    }
}
