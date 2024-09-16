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

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.JDAUtils;
import revxrsal.commands.jda.actor.SlashActorFactory;
import revxrsal.commands.jda.actor.SlashCommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.node.MutableExecutionContext;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static revxrsal.commands.exception.context.ErrorContext.parsingParameter;
import static revxrsal.commands.jda.JDAUtils.findCommand;
import static revxrsal.commands.jda.JDAUtils.toChoices;

public final class JDASlashListener<A extends SlashCommandActor> implements EventListener {

    private final Lamp<A> lamp;
    private final SlashActorFactory<A> actorFactory;

    public JDASlashListener(Lamp<A> lamp, SlashActorFactory<A> actorFactory) {
        this.lamp = lamp;
        this.actorFactory = actorFactory;
    }

    private void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        A actor = actorFactory.create(event, lamp);
        String fullPath = event.getFullCommandName();
        ExecutableCommand<A> command = findCommand(lamp, fullPath, event.getOptions()).orElseThrow();
        ExecutionContext<A> context = readArgumentsIntoContext(
                actor,
                command,
                event.getOptions(),
                event.getCommandString(),
                false
        );
        command.execute(context);
    }

    private void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        A actor = actorFactory.create(event, lamp);
        String fullPath = event.getFullCommandName();
        ExecutableCommand<A> command = findCommand(lamp, fullPath, event.getOptions()).orElse(null);
        if (command == null) {
            event.replyChoices().queue();
            return;
        }
        ExecutionContext<A> context = readArgumentsIntoContext(
                actor,
                command,
                event.getOptions(),
                event.getCommandString(),
                true
        );
        ParameterNode<A, ?> node = command.parameter(event.getFocusedOption().getName());
        var suggestions = node.suggestions().getSuggestions(context);

        List<Command.Choice> choices = toChoices(suggestions, event.getFocusedOption().getType());
        event.replyChoices(choices).queue();
    }

    private @NotNull ExecutionContext<A> readArgumentsIntoContext(
            @NotNull A actor,
            @NotNull ExecutableCommand<A> command,
            @NotNull List<OptionMapping> options,
            @NotNull String input,
            boolean ignoreExceptions
    ) {
        MutableExecutionContext<A> context = ExecutionContext.createMutable(command, actor, StringStream.create(input));
        Map<String, ParameterNode<A, ?>> parameters = new HashMap<>(command.parameters());
        for (OptionMapping option : options) {
            if (option.getType() == OptionType.SUB_COMMAND || option.getType() == OptionType.SUB_COMMAND_GROUP)
                continue;
            ParameterNode<A, ?> parameter = parameters.remove(option.getName());
            if (option.getType() == OptionType.STRING && parameter.type() != String.class) {
                MutableStringStream stream = StringStream.createMutable(option.getAsString());
                try {
                    Object value = parameter.parse(stream, context);
                    context.addResolvedArgument(option.getName(), parameter.type().cast(value));
                } catch (Throwable t) {
                    if (!ignoreExceptions)
                        lamp.handleException(t, parsingParameter(context, parameter, stream));
                }
                continue;
            }
            try {
                Object value = JDAUtils.fromOption(option, parameter);
                context.addResolvedArgument(option.getName(), value);
            } catch (Throwable t) {
                if (!ignoreExceptions)
                    lamp.handleException(t, parsingParameter(context, parameter, StringStream.createMutable(option.getAsString())));
            }
        }
        MutableStringStream empty = StringStream.createMutable("");
        if (!parameters.isEmpty()) {
            for (ParameterNode<A, ?> parameter : parameters.values()) {
                try {
                    Object value = parameter.parse(empty, context);
                    context.addResolvedArgument(parameter.name(), value);
                } catch (Throwable t) {
                    if (!ignoreExceptions)
                        lamp.handleException(t, parsingParameter(context, parameter, empty));
                }
            }
        }
        return context;
    }

    @Override public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof SlashCommandInteractionEvent slash) {
            onSlashCommandInteraction(slash);
        } else if (event instanceof CommandAutoCompleteInteractionEvent complete) {
            onCommandAutoCompleteInteraction(complete);
        }
    }
}
