package revxrsal.commands.jda.core;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.CommandPath;
import revxrsal.commands.jda.JDAActor;
import revxrsal.commands.jda.JDACommandHandler;
import revxrsal.commands.jda.annotation.OptionData;
import revxrsal.commands.jda.core.actor.BaseJDAMessageActor;
import revxrsal.commands.jda.core.actor.BaseJDASlashCommandActor;

@AllArgsConstructor
final class JDACommandListener implements EventListener {
    private final String prefix;
    private final JDACommandHandler handler;

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if (genericEvent instanceof MessageReceivedEvent)
            onMessageEvent((MessageReceivedEvent) genericEvent);
        if (genericEvent instanceof SlashCommandInteractionEvent)
            onSlashCommandEvent((SlashCommandInteractionEvent) genericEvent);
        if (genericEvent instanceof CommandAutoCompleteInteractionEvent)
            onAutocompleteEvent((CommandAutoCompleteInteractionEvent) genericEvent);
    }

    private void onAutocompleteEvent(CommandAutoCompleteInteractionEvent event) {
        Optional<ExecutableCommand> commandOptional = findExecutableCommand(event);
        if (!commandOptional.isPresent())
            return;
        ExecutableCommand command = commandOptional.get();
        AutoCompleteQuery focusedOption = event.getFocusedOption();
        Optional<CommandParameter> foundParameter = command.getValueParameters()
                .values()
                .stream()
                .filter(parameter -> getParameterName(parameter).equals(focusedOption.getName()))
                .findFirst();
        if (!foundParameter.isPresent())
            return;
        CommandParameter parameter = foundParameter.get();
        try {
            Collection<String> suggestions = parameter.getSuggestionProvider()
                    .getSuggestions(event.getOptions().stream().map(OptionMapping::getAsString).collect(Collectors.toList()), JDAActor.wrap(event, handler),
                            command);
            event.replyChoices(suggestions.stream().map(suggestion -> {
                if (focusedOption.getType() == OptionType.NUMBER)
                    return new Choice(suggestion, Double.parseDouble(suggestion));
                if (focusedOption.getType() == OptionType.INTEGER)
                    return new Choice(suggestion, Long.parseLong(suggestion));
                return new Choice(suggestion, suggestion);
            }).collect(Collectors.toList())).queue();
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    private void onSlashCommandEvent(SlashCommandInteractionEvent event) {
        parseSlashCommandEvent(event).ifPresent(content -> {
            JDAActor actor = new BaseJDASlashCommandActor(event, handler);
            try {
                ArgumentStack arguments = ArgumentStack.parse(content);
                handler.dispatch(actor, arguments);
            } catch(Throwable t) {
                handler.getExceptionHandler().handleException(t, actor);
            }
        });
    }

    private void onMessageEvent(MessageReceivedEvent event) {
        if (event.isWebhookMessage())
            return;
        String content = event.getMessage().getContentRaw();
        if (!content.startsWith(prefix))
            return;
        content = content.substring(prefix.length());
        if (content.isEmpty())
            return;

        JDAActor actor = new BaseJDAMessageActor(event, handler);
        try {
            ArgumentStack arguments = ArgumentStack.parse(content);
            handler.dispatch(actor, arguments);
        } catch(Throwable t) {
            handler.getExceptionHandler().handleException(t, actor);
        }
    }

    private Optional<ExecutableCommand> findExecutableCommand(CommandInteractionPayload event) {
        CommandPath commandPath = CommandPath.get(
                Stream.of(event.getName(), event.getSubcommandGroup(), event.getSubcommandName()).filter(Objects::nonNull).collect(Collectors.toList()));
        ExecutableCommand command = handler.getCommand(commandPath);
        if (command != null)
            return Optional.of(command);

        CommandCategory category = handler.getCategory(commandPath);
        if (category == null)
            return Optional.empty();
        return Optional.ofNullable(category.getDefaultAction());
    }

    private String getParameterName(CommandParameter parameter) {
        if (parameter.hasAnnotation(OptionData.class)) {
            OptionData optionData = parameter.getAnnotation(OptionData.class);
            return optionData.name().isEmpty() ? parameter.getName() : optionData.name();
        }
        return parameter.getName();
    }

    /**
     * Parses a SlashCommandInteractionEvent and converts event to a raw command string.
     *
     * @param event The SlashCommandInteractionEvent to parse.
     * @return An Optional containing the raw command string.
     */
    private Optional<String> parseSlashCommandEvent(SlashCommandInteractionEvent event) {
        if (event.getCommandType() != Type.SLASH)
            return Optional.of(event.getName());
        return findExecutableCommand(event).map(foundCommand -> {
            StringBuffer buffer = new StringBuffer();
            buffer.append(foundCommand.getPath().toRealString()).append(" ");

            Map<Integer, CommandParameter> valueParameters = foundCommand.getValueParameters();
            for (int i = 0; i < valueParameters.size(); i++) {
                CommandParameter parameter = valueParameters.get(i);
                OptionMapping optionMapping = event.getOption(getParameterName(parameter));
                if (optionMapping == null)
                    continue;
                if (parameter.isFlag())
                    buffer.append("-").append(parameter.getFlagName()).append(" ");
                if (parameter.isSwitch() && optionMapping.getType() == OptionType.BOOLEAN && optionMapping.getAsBoolean()) {
                    buffer.append("-").append(parameter.getSwitchName()).append(" ");
                    continue;
                }
                appendOptionMapping(buffer, optionMapping).append(" ");
            }

            return buffer.toString();
        });
    }

    private StringBuffer appendOptionMapping(StringBuffer buffer, OptionMapping optionMapping) {
        switch (optionMapping.getType()) {
            case CHANNEL:
                buffer.append(optionMapping.getAsChannel().getName());
                break;
            case USER:
                buffer.append(optionMapping.getAsUser().getName());
                break;
            case ROLE:
                buffer.append(optionMapping.getAsRole().getName());
                break;
            case MENTIONABLE:
                buffer.append("<@").append(optionMapping.getAsMentionable().getIdLong()).append(">");
                break;
            default:
                buffer.append(optionMapping.getAsString());
        }
        return buffer;
    }
}
