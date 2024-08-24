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
package revxrsal.commands.jda.parameters;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.parameter.ParameterType;

import static revxrsal.commands.util.Preconditions.cannotInstantiate;

/**
 * Contains {@link ParameterType}s for common types in JDA. These
 * will search for the snowflake ID first (which is often masked as a @mention),
 * then it will search by the name.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class SnowflakeParameterTypes {

    private SnowflakeParameterTypes() {
        cannotInstantiate(SnowflakeParameterTypes.class);
    }

    /**
     * Returns the {@link ParameterType} for parsing {@link Role} objects
     *
     * @param <A> The actor type
     * @return The parameter type
     */
    public static @NotNull <A extends CommandActor> ParameterType<A, Role> role() {
        return ((ParameterType) EnumSnowflakeResolvers.ROLE);
    }

    /**
     * Returns the {@link ParameterType} for parsing {@link Member} objects.
     *
     * @param <A> The actor type
     * @return The parameter type for members
     */
    public static @NotNull <A extends CommandActor> ParameterType<A, Member> member() {
        return ((ParameterType) EnumSnowflakeResolvers.MEMBER);
    }

    /**
     * Returns the {@link ParameterType} for parsing {@link User} objects.
     *
     * @param <A> The actor type
     * @return The parameter type for users
     */
    public static @NotNull <A extends CommandActor> ParameterType<A, User> user() {
        return ((ParameterType) EnumSnowflakeResolvers.UserResolver.USER);
    }

    /**
     * Returns the {@link ParameterType} for parsing {@link TextChannel} objects.
     *
     * @param <A> The actor type
     * @return The parameter type for text channels
     */
    public static @NotNull <A extends CommandActor> ParameterType<A, TextChannel> textChannel() {
        return ((ParameterType) EnumSnowflakeResolvers.TEXT_CHANNEL);
    }

    /**
     * Returns the {@link ParameterType} for parsing {@link NewsChannel} objects.
     *
     * @param <A> The actor type
     * @return The parameter type for news channels
     */
    public static @NotNull <A extends CommandActor> ParameterType<A, NewsChannel> newsChannel() {
        return ((ParameterType) EnumSnowflakeResolvers.NEWS_CHANNEL);
    }

    /**
     * Returns the {@link ParameterType} for parsing {@link ThreadChannel} objects.
     *
     * @param <A> The actor type
     * @return The parameter type for thread channels
     */
    public static @NotNull <A extends CommandActor> ParameterType<A, ThreadChannel> threadChannel() {
        return ((ParameterType) EnumSnowflakeResolvers.THREAD_CHANNEL);
    }

    /**
     * Returns the {@link ParameterType} for parsing {@link VoiceChannel} objects.
     *
     * @param <A> The actor type
     * @return The parameter type for voice channels
     */
    public static @NotNull <A extends CommandActor> ParameterType<A, VoiceChannel> voiceChannel() {
        return ((ParameterType) EnumSnowflakeResolvers.VOICE_CHANNEL);
    }

    /**
     * Returns the {@link ParameterType} for parsing {@link StageChannel} objects.
     *
     * @param <A> The actor type
     * @return The parameter type for stage channels
     */
    public static @NotNull <A extends CommandActor> ParameterType<A, StageChannel> stageChannel() {
        return ((ParameterType) EnumSnowflakeResolvers.STAGE_CHANNEL);
    }

    /**
     * Returns the {@link ParameterType} for parsing {@link Emoji} objects.
     *
     * @param <A> The actor type
     * @return The parameter type for emojis
     */
    public static @NotNull <A extends CommandActor> ParameterType<A, Emoji> emoji() {
        return ((ParameterType) EnumSnowflakeResolvers.EMOJI);
    }

    /**
     * Returns the {@link ParameterType} for parsing {@link Category} objects.
     *
     * @param <A> The actor type
     * @return The parameter type for categories
     */
    public static @NotNull <A extends CommandActor> ParameterType<A, Category> category() {
        return ((ParameterType) EnumSnowflakeResolvers.CATEGORY);
    }

    /**
     * Returns the {@link ParameterType} for parsing {@link ScheduledEvent} objects.
     *
     * @param <A> The actor type
     * @return The parameter type for scheduled events
     */
    public static @NotNull <A extends CommandActor> ParameterType<A, ScheduledEvent> scheduledEvent() {
        return ((ParameterType) EnumSnowflakeResolvers.SCHEDULED_EVENT);
    }
}
