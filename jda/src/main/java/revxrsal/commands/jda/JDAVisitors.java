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
package revxrsal.commands.jda;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.LampVisitor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.actor.SlashActorFactory;
import revxrsal.commands.jda.actor.SlashCommandActor;
import revxrsal.commands.jda.exception.SlashJDAExceptionHandler;
import revxrsal.commands.jda.sender.JDASenderResolver;
import revxrsal.commands.jda.slash.JDAParser;
import revxrsal.commands.jda.slash.JDASlashListener;
import revxrsal.commands.process.SenderResolver;

import static revxrsal.commands.jda.parameters.SnowflakeParameterTypes.*;

/**
 * Includes modular building blocks for hooking into the JDA
 * platform.
 * <p>
 * Accept individual functions using {@link Lamp.Builder#accept(LampBuilderVisitor)}
 */
public final class JDAVisitors {

    /**
     * Instructs Lamp to send all the currently registered commands to Discord
     *
     * @param actorFactory The actor factory. This allows for supplying custom implementations
     *                     of {@link SlashActorFactory}
     * @param jda          JDA instance to bind commands into
     * @return The visitor
     */
    public static <A extends SlashCommandActor> @NotNull LampVisitor<A> slashCommands(@NotNull JDA jda, @NotNull SlashActorFactory<A> actorFactory) {
        return lamp -> {
            JDAParser<A> parser = new JDAParser<>();
            for (ExecutableCommand<A> child : lamp.registry().children()) {
                parser.parse(child);
            }
            jda.updateCommands().addCommands(parser.commands().values()).queue();
            jda.addEventListener(new JDASlashListener<>(lamp, actorFactory));
        };
    }

    /**
     * Instructs Lamp to send all the currently registered commands to Discord
     *
     * @param jda JDA instance to bind commands into
     * @return The visitor
     */
    public static @NotNull LampVisitor<SlashCommandActor> slashCommands(@NotNull JDA jda) {
        return slashCommands(jda, SlashActorFactory.defaultFactory());
    }

    /**
     * Registers {@link SenderResolver}s for these parameters:
     * <ul>
     *     <li>{@link SlashCommandInteractionEvent}</li>
     *     <li>{@link User}</li>
     *     <li>{@link Member}</li>
     *     <li>{@link MessageChannel}</li>
     * </ul>
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends SlashCommandActor> @NotNull LampBuilderVisitor<A> jdaSenderResolver() {
        return builder -> builder.senderResolver(JDASenderResolver.INSTANCE);
    }

    /**
     * Registers response handlers for {@link MessageEmbed} and {@link EmbedBuilder}
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends SlashCommandActor> @NotNull LampBuilderVisitor<A> embedResponseHandlers() {
        return builder -> {
            builder.responseHandler(MessageEmbed.class, (response, context) ->
                    context.actor().channel().sendMessageEmbeds(response).queue()
            );
            builder.responseHandler(EmbedBuilder.class, (response, context) ->
                    context.actor().channel().sendMessageEmbeds(response.build()).queue()
            );
        };
    }

    /**
     * Registers the following types as context parameters:
     * <ul>
     *     <li>{@link JDA}</li>
     *     <li>{@link SelfUser}</li>
     *     <li>{@link Guild}</li>
     *     <li>{@link SlashCommandInteractionEvent}</li>
     * </ul>
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends SlashCommandActor> @NotNull LampBuilderVisitor<A> jdaContextParameters() {
        return builder -> builder.parameterTypes()
                .addContextParameterLast(SelfUser.class, (parameter, context) -> {
                    return context.actor().selfUser();
                })
                .addContextParameterLast(JDA.class, (parameter, context) -> {
                    return context.actor().jda();
                })
                .addContextParameterLast(Guild.class, (parameter, context) -> {
                    return context.actor().guild();
                })
                .addContextParameterLast(SlashCommandInteractionEvent.class, (parameter, context) -> {
                    return context.actor().commandEvent();
                });
    }

    /**
     * Registers parameter types for the following JDA types:
     * <ul>
     *     <li>{@link Role}</li>
     *     <li>{@link User}</li>
     *     <li>{@link Member}</li>
     *     <li>{@link TextChannel}</li>
     *     <li>{@link VoiceChannel}</li>
     *     <li>{@link StageChannel}</li>
     *     <li>{@link NewsChannel}</li>
     *     <li>{@link ThreadChannel}</li>
     *     <li>{@link ScheduledEvent}</li>
     *     <li>{@link Category}</li>
     *     <li>{@link Emoji}</li>
     * </ul>
     * As these types are identified by either a snowflake ID or a name, Lamp will first
     * try to use the snowflake ID (often masked as a @mention). If none is found, it will
     * look using the name instead.
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends SlashCommandActor> @NotNull LampBuilderVisitor<A> jdaParameterTypes() {
        return builder -> builder.parameterTypes()
                .addParameterTypeLast(Role.class, role())
                .addParameterTypeLast(User.class, user())
                .addParameterTypeLast(Member.class, member())
                .addParameterTypeLast(TextChannel.class, textChannel())
                .addParameterTypeLast(VoiceChannel.class, voiceChannel())
                .addParameterTypeLast(StageChannel.class, stageChannel())
                .addParameterTypeLast(ThreadChannel.class, threadChannel())
                .addParameterTypeLast(NewsChannel.class, newsChannel())
                .addParameterTypeLast(ScheduledEvent.class, scheduledEvent())
                .addParameterTypeLast(Emoji.class, emoji())
                .addParameterTypeLast(Category.class, category());
    }

    /**
     * Registers the default {@link revxrsal.commands.exception.CommandExceptionHandler} for JDA
     * exceptions
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends SlashCommandActor> @NotNull LampBuilderVisitor<A> jdaExceptionHandler() {
        return builder -> builder.exceptionHandler(new SlashJDAExceptionHandler<>());
    }
}
