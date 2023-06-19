package revxrsal.commands.jda.core;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.CommandPath;
import revxrsal.commands.jda.JDAActor;
import revxrsal.commands.jda.JDACommandHandler;
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

    /**
     * Parses a SlashCommandInteractionEvent and converts it to a raw command string.
     *
     * @param event The SlashCommandInteractionEvent to parse.
     * @return An Optional containing the raw command string.
     */
    private Optional<String> parseSlashCommandEvent(SlashCommandInteractionEvent event) {
        if (event.getCommandType() != Type.SLASH)
            return Optional.of(event.getName());
        CommandPath commandPath = CommandPath.get(
                Stream.of(event.getName(), event.getSubcommandGroup(), event.getSubcommandName()).filter(Objects::nonNull).collect(Collectors.toList()));

        ExecutableCommand foundCommand = findExecutableCommand(commandPath);
        if (foundCommand == null)
            return Optional.empty();
        StringBuffer buffer = new StringBuffer();
        buffer.append(commandPath.toRealString()).append(" ");

        Map<Integer, CommandParameter> valueParameters = foundCommand.getValueParameters();
        for (int i = 0; i < valueParameters.size(); i++) {
            CommandParameter parameter = valueParameters.get(i);
            OptionMapping optionMapping = event.getOption(parameter.getName());
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

        return Optional.of(buffer.toString());
    }

    private ExecutableCommand findExecutableCommand(CommandPath commandPath) {
        ExecutableCommand command = handler.getCommand(commandPath);
        if (command != null)
            return command;

        CommandCategory category = handler.getCategory(commandPath);
        if (category == null)
            return null;
        return category.getDefaultAction();
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
