package revxrsal.commands.jda;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.actor.MessageJDAActor;
import revxrsal.commands.jda.actor.SlashCommandJDAActor;
import revxrsal.commands.jda.actor.SlashCommandSuggestionJDAActor;
import revxrsal.commands.jda.core.actor.BaseJDACommandSuggestionActor;
import revxrsal.commands.jda.core.actor.BaseJDAMessageActor;
import revxrsal.commands.jda.core.actor.BaseJDASlashCommandActor;
import revxrsal.commands.jda.exception.GuildOnlyCommandException;
import revxrsal.commands.jda.exception.PrivateMessageOnlyCommandException;

/**
 * Represents a JDA {@link CommandActor} that executes a command,
 * whether in a private message or a guild.
 */
public interface JDAActor extends CommandActor {
    /**
     * Creates a new {@link JDAActor} that wraps the given {@link MessageReceivedEvent}.
     *
     * @param event Event to wrap
     * @return The wrapping {@link JDAActor}.
     */
    static @NotNull MessageJDAActor wrap(@NotNull MessageReceivedEvent event, @NotNull CommandHandler handler) {
        return new BaseJDAMessageActor(event, handler);
    }

    /**
     * Creates a new {@link JDAActor} that wraps the given {@link SlashCommandInteractionEvent}.
     *
     * @param event Event to wrap
     * @return The wrapping {@link JDAActor}.
     */
    static @NotNull SlashCommandJDAActor wrap(@NotNull SlashCommandInteractionEvent event, @NotNull CommandHandler handler) {
        return new BaseJDASlashCommandActor(event, handler);
    }

    /**
     * Creates a new {@link JDAActor} that wraps the given {@link CommandAutoCompleteInteractionEvent}.
     *
     * @param event Event to wrap
     * @return The wrapping {@link JDAActor}.
     */
    static @NotNull SlashCommandSuggestionJDAActor wrap(@NotNull CommandAutoCompleteInteractionEvent event, @NotNull CommandHandler handler) {
        return new BaseJDACommandSuggestionActor(event, handler);
    }

    /**
     * Returns the snowflake ID of this actor
     *
     * @return The snowflake ID
     */
    long getIdLong();

    /**
     * Returns the snowflake ID of this actor as a string.
     *
     * @return The snowflake ID
     */
    @NotNull String getId();

    /**
     * Returns the underlying {@link User} of this actor
     *
     * @return The underlying user
     */
    @NotNull User getUser();

    /**
     * Returns the message of the actor
     *
     * @return The actor's sent message
     * @deprecated As of Lamp 3.2.0, because of support slash commands use {@link MessageJDAActor#getMessage()}
     */
    @Deprecated
    default @Nullable Message getMessage() {
        return null;
    }

    /**
     * Returns the {@link Event} that created this actor.
     *
     * @return The event
     */
    @NotNull Event getGenericEvent();

    /**
     * Returns the {@link MessageReceivedEvent} that created this
     * actor.
     *
     * @return The event
     * @deprecated As of Lamp 3.2.0, because of support slash commands use
     * {@link #getGenericEvent()} or {@link MessageJDAActor#getEvent()} instead.
     */
    @Deprecated
    default @Nullable MessageReceivedEvent getEvent() {
        return null;
    }

    /**
     * Returns the channel this actor sent the command in
     *
     * @return The channel
     */
    @NotNull MessageChannel getChannel();

    /**
     * Returns whether this actor sent a command in a guild or not
     *
     * @return If this command was run in a guild
     */
    boolean isGuildEvent();

    /**
     * Returns the guild of this actor, or throws a {@link IllegalStateException}
     * if the command was not run in a guild.
     *
     * @return The guild
     */
    @NotNull Guild getGuild();

    /**
     * Returns the {@link Member} of this actor. This will throw an exception
     * in case of private messages. Check with {@link #isGuildEvent()}.
     *
     * @return the member of the actor
     */
    @NotNull Member getMember();

    /**
     * Returns this actor if it is in a guild, otherwise throws a
     * {@link GuildOnlyCommandException}.
     *
     * @param command Command to check for.
     * @return This actor
     */
    JDAActor checkInGuild(ExecutableCommand command) throws GuildOnlyCommandException;

    /**
     * Returns this actor if it is not in a guild, otherwise throws a
     * {@link PrivateMessageOnlyCommandException}.
     *
     * @param command Command to check for.
     * @return This actor
     */
    JDAActor checkNotInGuild(ExecutableCommand command) throws PrivateMessageOnlyCommandException;
}
