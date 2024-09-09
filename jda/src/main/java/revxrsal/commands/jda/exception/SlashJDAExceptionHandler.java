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
package revxrsal.commands.jda.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.CommandInvocationException;
import revxrsal.commands.exception.DefaultExceptionHandler;
import revxrsal.commands.exception.NoPermissionException;
import revxrsal.commands.jda.actor.SlashCommandActor;

import java.util.Locale;

import static revxrsal.commands.util.BuiltInNamingStrategies.separateCamelCase;

public class SlashJDAExceptionHandler<A extends SlashCommandActor> extends DefaultExceptionHandler<A> {

    @HandleException
    public void onMemberNotInGuild(MemberNotInGuildException e, SlashCommandActor actor) {
        actor.commandEvent().reply("🛑 User **" + e.suppliedUser().getEffectiveName() + "** is not in this guild.").queue();
    }

    @HandleException
    public void onWrongChannelType(WrongChannelTypeException e, SlashCommandActor actor) {
        String typeName = e.expectedType().getSimpleName();
        String exp = separateCamelCase(typeName, " ").toLowerCase(Locale.ENGLISH);
        String rec = e.channel().getType().name().toLowerCase().replace('_', ' ');
        actor.commandEvent().reply("🛑 Wrong channel type. Expected a **" + exp + "**, received a **" + rec + "**.").queue();
    }

    @HandleException
    public void onGuildOnlyCommand(GuildOnlyCommandException e, SlashCommandActor actor) {
        actor.commandEvent().reply("🛑 This command can only be used in guilds").queue();
    }

    @Override public void onCommandInvocation(@NotNull CommandInvocationException e, @NotNull A actor) {
        actor.commandEvent().reply("🛑 An error has occurred while executing this command. Please contact the developers." +
                " Errors have been printed to the console.").queue();
        e.cause().printStackTrace();
    }

    @Override public void onNoPermission(@NotNull NoPermissionException e, @NotNull A actor) {
        actor.replyToInteraction("🛑 You do not have permission to execute this command!").queue();
    }

    @HandleException
    public void onInvalidCategory(InvalidCategoryException e, SlashCommandActor actor) {
        actor.error("**Invalid role:** " + e.input());
    }

    @HandleException
    public void onInvalidChannel(InvalidChannelException e, SlashCommandActor actor) {
        actor.error("**Invalid channel:** " + e.input());
    }

    @HandleException
    public void onInvalidEmoji(InvalidEmojiException e, SlashCommandActor actor) {
        actor.error("**Invalid emote:** " + e.input());
    }

    @HandleException
    public void onInvalidRole(InvalidRoleException e, SlashCommandActor actor) {
        actor.error("**Invalid role:** " + e.input());
    }

    @HandleException
    public void onInvalidUser(InvalidUserException e, SlashCommandActor actor) {
        actor.error("**Invalid user:** " + e.input());
    }

    @HandleException
    public void onInvalidScheduledEvent(InvalidScheduledEventException e, SlashCommandActor actor) {
        actor.error("**Invalid scheduled event:** " + e.input());
    }
}
