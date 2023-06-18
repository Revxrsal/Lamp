package revxrsal.commands.jda.actor;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import revxrsal.commands.jda.JDAActor;

public interface MessageJDAActor extends JDAActor {
    /**
     * Returns the message of the actor
     *
     * @return The actor's sent message
     */
    @Override
    default @NotNull Message getMessage() {
        return getEvent().getMessage();
    }

    /**
     * Returns the {@link MessageReceivedEvent} that created this
     * actor.
     *
     * @return The event
     */
    @Override
    default @NotNull MessageReceivedEvent getEvent() {
        return (MessageReceivedEvent) getGenericEvent();
    }
}
