package revxrsal.commands.jda.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.SlashCommandMapper;
import revxrsal.commands.jda.annotation.GuildOnly;
import revxrsal.commands.jda.annotation.GuildPermission;
import revxrsal.commands.jda.annotation.NSFW;
import revxrsal.commands.jda.annotation.OptionChoice;
import revxrsal.commands.jda.annotation.OptionData;
import revxrsal.commands.jda.core.adapter.SlashCommandAdapter;
import revxrsal.commands.util.Primitives;

public class BasicSlashCommandMapper implements SlashCommandMapper {
    @Override
    public void mapSlashCommand(@NotNull SlashCommandAdapter slashCommandAdapter, @NotNull ExecutableCommand command) {
        if (slashCommandAdapter.isSlashCommand()) {
            SlashCommandData slashCommandData = slashCommandAdapter.getCommandData();
            if (command.hasAnnotation(GuildOnly.class))
                slashCommandData.setGuildOnly(true);
            if (command.hasAnnotation(GuildPermission.class))
                slashCommandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.getAnnotation(GuildPermission.class).value()));
            if (command.hasAnnotation(NSFW.class))
                slashCommandData.setNSFW(true);
        }
        Map<Integer, CommandParameter> valueParameters = command.getValueParameters();
        for (int i = 0; i < valueParameters.size(); i++) {
            CommandParameter parameter = valueParameters.get(i);
            slashCommandAdapter.addOptions(createOptionData(parameter));
        }
    }

    private net.dv8tion.jda.api.interactions.commands.build.OptionData createOptionData(CommandParameter parameter) {
        String parameterDescription = Optional.ofNullable(parameter.getDescription()).orElse(parameter.getName());
        if (parameter.hasAnnotation(OptionData.class)) {
            OptionData optionDataAnnotation = parameter.getAnnotation(OptionData.class);
            OptionType type = optionDataAnnotation.value();
            String name = optionDataAnnotation.name().isEmpty() ? parameter.getName() : optionDataAnnotation.name();
            String description = optionDataAnnotation.description().isEmpty() ? parameterDescription : optionDataAnnotation.description();
            if (!type.canSupportChoices() && optionDataAnnotation.choices().length != 0) {
                String choiceSupportedTypes = Arrays.stream(OptionType.values())
                        .filter(OptionType::canSupportChoices)
                        .map(OptionType::name)
                        .collect(Collectors.joining(" "));
                throw new IllegalArgumentException("Type " + type.name() + " doesn't support choices! Consider using: '" + choiceSupportedTypes + "'");
            }
            net.dv8tion.jda.api.interactions.commands.build.OptionData optionData = new net.dv8tion.jda.api.interactions.commands.build.OptionData(type, name,
                    description, optionDataAnnotation.required(), optionDataAnnotation.autocomplete());
            if (optionDataAnnotation.choices().length != 0)
                optionData.addChoices(createChoices(type, optionDataAnnotation.choices()));
            return optionData;
        }
        String name = parameter.getName();
        boolean required = !parameter.isOptional();
        if (parameter.isSwitch())
            return new net.dv8tion.jda.api.interactions.commands.build.OptionData(OptionType.BOOLEAN, name, parameterDescription, false);
        OptionType type = findType(parameter);
        Collection<Choice> choices = Collections.emptyList();
        if (Enum.class.isAssignableFrom(parameter.getType())) {
            Enum<?>[] enums = (Enum<?>[]) parameter.getType().getEnumConstants();
            choices = Arrays.stream(enums).map(Enum::name).map(enumName -> new Choice(enumName, enumName)).collect(Collectors.toList());
        }
        net.dv8tion.jda.api.interactions.commands.build.OptionData optionData = new net.dv8tion.jda.api.interactions.commands.build.OptionData(type, name,
                parameterDescription, required);
        if (!choices.isEmpty())
            optionData.addChoices(choices);
        return optionData;
    }

    private OptionType findType(CommandParameter parameter) {
        Class<?> type = Primitives.wrap(parameter.getType());
        if (Integer.class.isAssignableFrom(type) || Long.class.isAssignableFrom(type) || Short.class.isAssignableFrom(type) ||
                Byte.class.isAssignableFrom(type))
            return OptionType.INTEGER;
        if (Double.class.isAssignableFrom(type) || Float.class.isAssignableFrom(type))
            return OptionType.NUMBER;
        if (Boolean.class.isAssignableFrom(type))
            return OptionType.BOOLEAN;
        if (Member.class.isAssignableFrom(type) || User.class.isAssignableFrom(type))
            return OptionType.USER;
        if (TextChannel.class.isAssignableFrom(type) || VoiceChannel.class.isAssignableFrom(type) || StageChannel.class.isAssignableFrom(type))
            return OptionType.CHANNEL;
        if (Role.class.isAssignableFrom(type))
            return OptionType.ROLE;
        return OptionType.STRING;
    }

    private Collection<Choice> createChoices(OptionType type, OptionChoice[] choices) {
        return Arrays.stream(choices).map(choiceAnnotation -> {
            if (type == OptionType.NUMBER)
                return new Choice(choiceAnnotation.name(), Double.parseDouble(choiceAnnotation.value()));
            if (type == OptionType.INTEGER)
                return new Choice(choiceAnnotation.name(), Long.parseLong(choiceAnnotation.value()));
            return new Choice(choiceAnnotation.name(), choiceAnnotation.value());
        }).collect(Collectors.toList());
    }
}
