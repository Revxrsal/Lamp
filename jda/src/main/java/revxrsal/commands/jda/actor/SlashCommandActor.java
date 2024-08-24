/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.jda.actor;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.jda.exception.GuildOnlyCommandException;

/**
 * Represents a {@link CommandActor} that originated from a {@link SlashCommandInteractionEvent slash command},
 * or from an {@link CommandAutoCompleteInteractionEvent auto-complete request}.
 */
public interface SlashCommandActor extends CommandActor {

    /**
     * Returns the underlying {@link User}
     *
     * @return The underlying {@link User}
     */
    @NotNull User user();

    /**
     * Returns the underlying event
     *
     * @return The underlying event
     */
    @NotNull GenericInteractionCreateEvent event();

    /**
     * Returns the JDA instance
     *
     * @return The JDA instance
     */
    default @NotNull JDA jda() {
        return event().getJDA();
    }

    /**
     * Returns the currently logged-in user
     *
     * @return The currently logged-in user
     * @see SelfUser
     */
    default @NotNull SelfUser selfUser() {
        return jda().getSelfUser();
    }

    /**
     * Returns the guild the command was executed in, otherwise returns
     * {@code null}
     *
     * @return The guild, or {@code null} if not in a guild.
     */
    default @Nullable Guild guildOrNull() {
        return event().getGuild();
    }


    /**
     * Returns the guild the command was executed in, otherwise throws
     * a {@link GuildOnlyCommandException}
     *
     * @return The guild
     * @throws GuildOnlyCommandException if not in a guild
     */
    default @NotNull Guild guild() {
        Guild guild = event().getGuild();
        if (guild == null)
            throw new GuildOnlyCommandException();
        return guild;
    }

    /**
     * Returns the {@link MessageChannel} that this interaction ocurred in
     *
     * @return The channel
     */
    default @NotNull MessageChannel channel() {
        return event().getMessageChannel();
    }

    /**
     * Returns this event as a {@link SlashCommandInteractionEvent} if it is from a
     * slash command, otherwise throws a {@link IllegalArgumentException}.
     *
     * @return The event as a {@link SlashCommandInteractionEvent}
     * @throws IllegalArgumentException if it is not a {@link SlashCommandInteractionEvent}
     */
    default @NotNull SlashCommandInteractionEvent commandEvent() {
        if (event() instanceof SlashCommandInteractionEvent)
            return (SlashCommandInteractionEvent) event();
        throw new IllegalArgumentException("The event is not a SlashCommandInteractionEvent!");
    }

    /**
     * Returns this event as a {@link SlashCommandInteractionEvent} if it is from a
     * slash command, otherwise returns {@code null}.
     *
     * @return The event as a {@link SlashCommandInteractionEvent}, or null.
     */
    default @Nullable SlashCommandInteractionEvent commandEventOrNull() {
        if (event() instanceof SlashCommandInteractionEvent)
            return (SlashCommandInteractionEvent) event();
        return null;
    }

    /**
     * Returns this event as a {@link CommandAutoCompleteInteractionEvent} if it is from a
     * slash command, otherwise throws a {@link IllegalArgumentException}.
     *
     * @return The event as a {@link CommandAutoCompleteInteractionEvent}
     * @throws IllegalArgumentException if it is not a {@link CommandAutoCompleteInteractionEvent}
     */
    default @NotNull CommandAutoCompleteInteractionEvent autoCompleteEvent() {
        if (event() instanceof CommandAutoCompleteInteractionEvent)
            return (CommandAutoCompleteInteractionEvent) event();
        throw new IllegalArgumentException("The event is not a CommandAutoCompleteInteractionEvent!");
    }

    /**
     * Returns this event as a {@link CommandAutoCompleteInteractionEvent} if it is from an
     * auto-complete events, otherwise returns {@code null}.
     *
     * @return The event as a {@link CommandAutoCompleteInteractionEvent}, or null.
     */
    default @Nullable CommandAutoCompleteInteractionEvent autoCompleteEventOrNull() {
        if (event() instanceof CommandAutoCompleteInteractionEvent)
            return (CommandAutoCompleteInteractionEvent) event();
        return null;
    }

    /**
     * Reply to this interaction and acknowledge it.
     *
     * @param content The message content to send
     * @return {@link ReplyCallbackAction}
     * @see IReplyCallback#reply(String)
     */
    @CheckReturnValue
    @Contract(pure = true)
    default ReplyCallbackAction replyToInteraction(@NotNull String content) {
        return commandEvent().reply(content);
    }

    /**
     * Reply to this interaction and acknowledge it.
     *
     * @param content The message content to send
     * @return {@link ReplyCallbackAction}
     * @see IReplyCallback#reply(String)
     */
    @CheckReturnValue
    @Contract(pure = true)
    default ReplyCallbackAction replyToInteraction(@NotNull MessageCreateData content) {
        return commandEvent().reply(content);
    }

    /**
     * Acknowledge this interaction and defer the reply to a later time.
     * <br>This will send a {@code <Bot> is thinking...} message in chat that will be updated later through either {@link InteractionHook#editOriginal(String)} or {@link InteractionHook#sendMessage(String)}.
     *
     * @param ephemeral True, if this message should only be visible to the interaction user
     * @return {@link ReplyCallbackAction}
     * @see IReplyCallback#deferReply(boolean)
     */
    @CheckReturnValue
    @Contract(pure = true)
    default ReplyCallbackAction deferReply(boolean ephemeral) {
        return commandEvent().deferReply(ephemeral);
    }

    /**
     * Acknowledge this interaction and defer the reply to a later time.
     * <br>This will send a {@code <Bot> is thinking...} message in chat that will be updated later through either {@link InteractionHook#editOriginal(String)} or {@link InteractionHook#sendMessage(String)}.
     *
     * @return {@link ReplyCallbackAction}
     * @see IReplyCallback#deferReply()
     */
    @CheckReturnValue
    @Contract(pure = true)
    default ReplyCallbackAction deferReply() {
        return commandEvent().deferReply();
    }

    /**
     * The {@link InteractionHook} which can be used to send deferred replies or followup messages.
     *
     * @return The interaction hook
     */
    @CheckReturnValue
    @Contract(pure = true)
    default @NotNull InteractionHook hook() {
        return commandEvent().getHook();
    }

    /**
     * Returns the {@link Lamp} instance that constructed this actor.
     *
     * @return The lamp instance
     */
    @Override Lamp<SlashCommandActor> lamp();

}
