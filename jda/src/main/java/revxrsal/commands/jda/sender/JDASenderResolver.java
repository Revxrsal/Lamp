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
package revxrsal.commands.jda.sender;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.actor.SlashCommandActor;
import revxrsal.commands.jda.exception.GuildOnlyCommandException;
import revxrsal.commands.process.SenderResolver;

public enum JDASenderResolver implements SenderResolver<SlashCommandActor> {
    INSTANCE;

    @Override public boolean isSenderType(@NotNull CommandParameter parameter) {
        Class<?> type = parameter.type();
        return SlashCommandInteractionEvent.class.isAssignableFrom(type)
                || User.class.isAssignableFrom(type)
                || Member.class.isAssignableFrom(type)
                || MessageChannel.class.isAssignableFrom(type);
    }

    @Override
    public @NotNull Object getSender(@NotNull Class<?> type, @NotNull SlashCommandActor actor, @NotNull ExecutableCommand<SlashCommandActor> command) {
        if (SlashCommandInteractionEvent.class.isAssignableFrom(type))
            return actor.commandEvent();
        if (User.class.isAssignableFrom(type))
            return actor.user();
        if (Member.class.isAssignableFrom(type)) {
            Member member = actor.event().getMember();
            if (member != null)
                return member;
            throw new GuildOnlyCommandException();
        }
        if (MessageChannel.class.isAssignableFrom(type)) {
            return actor.event().getMessageChannel();
        }
        return actor.commandEvent();
    }
}
