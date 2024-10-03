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
package revxrsal.commands.jda.slash;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.actor.SlashCommandActor;
import revxrsal.commands.jda.annotation.CommandPermission;
import revxrsal.commands.jda.annotation.GuildOnly;
import revxrsal.commands.jda.annotation.NSFW;
import revxrsal.commands.node.CommandNode;
import revxrsal.commands.node.ParameterNode;

import java.util.HashMap;
import java.util.Map;

import static revxrsal.commands.jda.JDAUtils.toOptionData;

public final class JDAParser<A extends SlashCommandActor> {

    private final Map<String, SlashCommandData> commands = new HashMap<>();

    public void parse(@NotNull ExecutableCommand<A> executable) {
        checkCommand(executable);
        String name = executable.firstNode().name();
        String description = executable.description() == null ? executable.path() : executable.description();

        SlashCommandData slash = slash(name, description);
        slash.setNSFW(executable.annotations().contains(NSFW.class));
        slash.setGuildOnly(executable.annotations().contains(GuildOnly.class));
        Permission[] permissions = executable.annotations().map(CommandPermission.class, CommandPermission::value);
        if (permissions != null)
            slash.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions));

        if (executable.size() == 2) {
            CommandNode<A> node = executable.lastNode();
            if (node.isParameter()) {
                ParameterNode<A, Object> parameter = node.requireParameterNode();
                slash.addOptions(toOptionData(parameter));
            } else {
                slash.addSubcommands(new SubcommandData(node.name(), description));
            }
        } else if (executable.size() >= 3) {
            CommandNode<A> secondNode = executable.nodes().get(1);
            if (secondNode.isParameter()) {
                for (int i = 1; i < executable.nodes().size(); i++) {
                    ParameterNode<A, Object> parameter = executable.nodes().get(i).requireParameterNode();
                    slash.addOptions(toOptionData(parameter));
                }
            } else {
                CommandNode<A> thirdNode = executable.nodes().get(2);
                if (thirdNode.isParameter()) {
                    SubcommandData subcommandData = new SubcommandData(executable.nodes().get(1).name(), description);
                    for (int i = 2; i < executable.nodes().size(); i++) {
                        ParameterNode<A, Object> parameter = executable.nodes().get(i).requireParameterNode();
                        subcommandData.addOptions(toOptionData(parameter));
                    }
                    slash.addSubcommands(subcommandData);
                } else {
                    SubcommandGroupData group = new SubcommandGroupData(executable.nodes().get(1).name(), description);
                    SubcommandData subcommand = new SubcommandData(thirdNode.name(), description);
                    for (int i = 2; i < executable.nodes().size(); i++) {
                        ParameterNode<A, Object> parameter = executable.nodes().get(i).requireParameterNode();
                        subcommand.addOptions(toOptionData(parameter));
                    }
                    group.addSubcommands(subcommand);
                    slash.addSubcommandGroups(group);
                }
            }
        }
    }

    private void checkCommand(@NotNull ExecutableCommand<A> executable) {
        boolean startedParameters = false;
        boolean canUseLiterals = true;
        int i = 0;
        for (CommandNode<A> node : executable.nodes()) {
            if (i > 2)
                canUseLiterals = false;
            if (node.isLiteral()) {
                if (startedParameters)
                    throw new IllegalArgumentException("You cannot have parameters between literals in slash commands! Found literal '" + node.name() + "'");
                if (!canUseLiterals)
                    throw new IllegalArgumentException("You can only have 3 consecutive literals in slash commands!");
            }
            if (node instanceof ParameterNode) {
                ParameterNode<A, ?> parameter = (ParameterNode<A, ?>) node;
                if (parameter.isSwitch())
                    throw new IllegalArgumentException("Switches are not supported in JDA slash commands.");
                if (parameter.isFlag())
                    throw new IllegalArgumentException("Flag parameters are not supported in JDA slash commands.");
                startedParameters = true;
            }
            i += 1;
        }
    }

    private @NotNull SlashCommandData slash(@NotNull String name, @Nullable String description) {
        return commands.computeIfAbsent(name, k -> Commands.slash(k, description == null ? k : description));
    }

    public @NotNull Map<String, SlashCommandData> commands() {
        return commands;
    }
}