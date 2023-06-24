package revxrsal.commands.jda.actor;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import revxrsal.commands.jda.JDAActor;

public interface SlashCommandJDAActor extends JDAActor {
    /**
     * Returns the ReplyCallbackAction. You should use {@link ReplyCallbackAction#queue()}, for sending 'thinking...' action
     *
     * @return The actor's message channel union
     */
    default @NotNull ReplyCallbackAction deferReply() {
        return getSlashEvent().deferReply();
    }

    /**
     * Returns the messsage channel union of the actor
     *
     * @return The actor's message channel union
     */
    default @NotNull MessageChannelUnion getChannelUnion() {
        return getSlashEvent().getChannel();
    }

    /**
     * Returns the interaction of the actor
     *
     * @return The actor's sent message
     */
    default @NotNull SlashCommandInteraction getInteraction() {
        return getSlashEvent().getInteraction();
    }

    /**
     * Returns the cast result of {@link #getGenericEvent()} to {@link SlashCommandInteractionEvent}.
     *
     * @return The event
     */
    default @NotNull SlashCommandInteractionEvent getSlashEvent() {
        return (SlashCommandInteractionEvent) getGenericEvent();
    }
}
