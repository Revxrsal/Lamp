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

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Length;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.actor.SlashCommandActor;
import revxrsal.commands.jda.annotation.Choices;
import revxrsal.commands.jda.annotation.NSFW;
import revxrsal.commands.jda.exception.MemberNotInGuildException;
import revxrsal.commands.jda.exception.WrongChannelTypeException;
import revxrsal.commands.node.CommandNode;
import revxrsal.commands.node.ParameterNode;

import java.util.*;

import static net.dv8tion.jda.api.interactions.commands.build.OptionData.MAX_CHOICES;
import static revxrsal.commands.util.Classes.wrap;
import static revxrsal.commands.util.Collections.map;
import static revxrsal.commands.util.Preconditions.cannotInstantiate;

/**
 * Includes utilities for the JDA platform
 */
public final class JDAUtils {

    private static final Map<Class<? extends Channel>, ChannelType> CHANNEL_TYPES = new HashMap<>();

    static {
        for (ChannelType value : ChannelType.values()) {
            CHANNEL_TYPES.put(value.getInterface(), value);
        }
    }

    private JDAUtils() {
        cannotInstantiate(JDAUtils.class);
    }

    /**
     * Finds the most suitable {@link ExecutableCommand} from the given path and options
     *
     * @param lamp     The {@link Lamp} instance
     * @param fullPath The full path of literals
     * @param options  The supplied list of options
     * @param <A>      The actor type
     * @return The command, or {@link Optional#empty()} if not found.
     */
    public static <A extends CommandActor> @NotNull Optional<ExecutableCommand<A>> findCommand(
            @NotNull Lamp<A> lamp,
            @NotNull String fullPath,
            @NotNull List<OptionMapping> options
    ) {
        List<ExecutableCommand<A>> potential = new ArrayList<>();
        for (ExecutableCommand<A> command : lamp.registry().children()) {
            if (getRequiredPath(command).equals(fullPath)) {
                potential.add(command);
            }
        }
        if (potential.isEmpty()) {
            return Optional.empty();
        }
        if (potential.size() == 1)
            return Optional.of(potential.get(0));
        for (Iterator<ExecutableCommand<A>> iterator = potential.iterator(); iterator.hasNext(); ) {
            ExecutableCommand<A> command = iterator.next();
            Map<String, ParameterNode<A, ?>> parameters = getParameters(command);
            for (OptionMapping option : options) {
                if (!parameters.containsKey(option.getName()))
                    iterator.remove();
            }
        }
        if (potential.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(potential.get(0));
    }

    /**
     * Parses the object inside the {@link OptionMapping} safely to be
     * fed into a {@link ParameterNode}
     *
     * @param option    The option to parse from
     * @param parameter The parameter to adapt for
     * @param <A>       The actor type
     * @return The resolved value
     */
    public static <A extends CommandActor> @NotNull Object fromOption(
            @NotNull OptionMapping option,
            @NotNull ParameterNode<A, ?> parameter
    ) {
        switch (option.getType()) {
            case STRING -> {
                return option.getAsString();
            }
            case INTEGER -> {
                return safeIntegerCast(option.getAsInt(), parameter.type());
            }
            case NUMBER -> {
                return safeDoubleCast(option.getAsInt(), parameter.type());
            }
            case BOOLEAN -> {
                return option.getAsBoolean();
            }
            case USER -> {
                if (parameter.type() == Member.class) {
                    Member asMember = option.getAsMember();
                    if (asMember == null) {
                        throw new MemberNotInGuildException(option.getAsUser());
                    }
                }
                return option.getAsUser();
            }
            case CHANNEL -> {
                if (option.getChannelType().getInterface().isAssignableFrom(parameter.type()))
                    return option.getAsChannel();
                throw new WrongChannelTypeException(option.getAsChannel(), parameter.type());
            }
            case ROLE -> {
                return option.getAsRole();
            }
            case MENTIONABLE -> {
                return option.getAsMentionable();
            }
            case ATTACHMENT -> {
                return option.getAsAttachment();
            }
        }
        throw new IllegalArgumentException("Don't know how to fetch a value from Option: " + option + " for parameter " + parameter.name() + " of type " + parameter.type());
    }

    private static @NotNull Object safeIntegerCast(int value, Class<?> type) {
        if (type == int.class)
            return value;
        if (type == short.class)
            return (short) value;
        if (type == byte.class)
            return (byte) value;
        if (type == long.class)
            return (long) value;
        return value;
    }

    private static @NotNull Object safeDoubleCast(double value, Class<?> type) {
        if (type == double.class)
            return value;
        if (type == float.class)
            return (float) value;
        return value;
    }

    /**
     * Creates {@link OptionData} from the given {@link ParameterNode}. This will respect
     * the following annotations:
     * <ul>
     *     <li>{@link NSFW @NSFW}</li>
     *     <li>{@link Range @Range}</li>
     *     <li>{@link Length @Length}</li>
     *     <li>{@link Choices @Choices}</li>
     * </ul>
     * If the parameter type is an enum, it will also use its values
     * as predefined choices
     *
     * @param parameter The parameter to parse for
     * @param <A>       The actor type
     * @return The created {@link OptionData}.
     */
    @Contract(value = "_ -> new", pure = true)
    public static <A extends SlashCommandActor> @NotNull OptionData toOptionData(@NotNull ParameterNode<A, Object> parameter) {
        OptionData data = new OptionData(
                toOptionType(parameter),
                parameter.name(),
                parameter.description() == null ? parameter.name() : parameter.description()
        );
        data.setRequired(parameter.isRequired());
        if (!parameter.suggestions().equals(SuggestionProvider.empty()))
            data.setAutoComplete(true);
        Range range = parameter.annotations().get(Range.class);
        Class<?> paramType = wrap(parameter.type());
        if (range != null) {
            if (paramType == Double.class || paramType == Float.class)
                data.setRequiredRange(range.min(), range.max());
            else
                data.setRequiredRange((long) range.min(), (long) range.max());
        } else {
            if (paramType == Integer.class)
                data.setRequiredRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
            else if (paramType == Short.class)
                data.setRequiredRange(Short.MIN_VALUE, Short.MAX_VALUE);
            else if (paramType == Byte.class)
                data.setRequiredRange(Byte.MIN_VALUE, Byte.MAX_VALUE);
            else if (paramType == Float.class)
                data.setRequiredRange(Float.MIN_VALUE, Float.MAX_VALUE);
        }
        Length length = parameter.annotations().get(Length.class);
        if (length != null)
            data.setRequiredLength(length.min(), length.max());

        if (Channel.class.isAssignableFrom(parameter.type())) {
            ChannelType channelType = channelType(parameter.type().asSubclass(Channel.class));
            if (channelType != null)
                data.setChannelTypes(channelType);
        }
        Choice[] choices = mapToChoices(parameter.annotations().get(Choices.class), data.getType());
        if (choices != null) {
            data.setAutoComplete(false);
            data.addChoices(choices);
        } else if (parameter.type().isEnum()) {
            data.setAutoComplete(false);
            Enum<?>[] enums = (Enum<?>[]) parameter.type().getEnumConstants();
            for (int i = 0; i < Math.min(enums.length, MAX_CHOICES); i++) {
                String value = enums[i].name().toLowerCase();
                data.addChoice(value, value);
            }
        }
        return data;
    }

    /**
     * Maps the values inside a {@link Choices @Choices} annotation into {@link Choice choices}
     *
     * @param suggest Suggest annotation
     * @param type    The option type
     * @return The choices array
     */
    @Contract("null, _ -> null")
    private static Choice[] mapToChoices(@Nullable Choices suggest, @NotNull OptionType type) {
        if (suggest == null)
            return null;
        String[] suggests = suggest.value();
        Choice[] values = new Choice[suggests.length];
        for (int i = 0; i < Math.min(suggests.length, MAX_CHOICES); i++) {
            String s = suggests[i];
            values[i] = toChoice(s, type);
        }
        return values;
    }

    /**
     * Finds a suitable {@link OptionType} for the given {@link ParameterNode}. This will
     * never return null. In cases of an odd parameter type, this will return
     * an {@link OptionType#STRING}.
     *
     * @param parameter The parameter to find for
     * @param <A>       The actor type
     * @return The best option type
     */
    public static <A extends SlashCommandActor> @NotNull OptionType toOptionType(ParameterNode<A, Object> parameter) {
        Class<?> type = wrap(parameter.type());
        if (Integer.class.isAssignableFrom(type)
                || Long.class.isAssignableFrom(type)
                || Short.class.isAssignableFrom(type)
                || Byte.class.isAssignableFrom(type))
            return OptionType.INTEGER;
        if (Double.class.isAssignableFrom(type) || Float.class.isAssignableFrom(type))
            return OptionType.NUMBER;
        if (Boolean.class.isAssignableFrom(type))
            return OptionType.BOOLEAN;
        if (Member.class.isAssignableFrom(type) || User.class.isAssignableFrom(type))
            return OptionType.USER;
        if (Message.Attachment.class.isAssignableFrom(type))
            return OptionType.ATTACHMENT;
        if (TextChannel.class.isAssignableFrom(type)
                || VoiceChannel.class.isAssignableFrom(type)
                || StageChannel.class.isAssignableFrom(type)
                || NewsChannel.class.isAssignableFrom(type)
        )
            return OptionType.CHANNEL;
        if (Role.class.isAssignableFrom(type))
            return OptionType.ROLE;
        if (IMentionable.class.isAssignableFrom(type))
            return OptionType.MENTIONABLE;
        return OptionType.STRING;
    }

    /**
     * Maps the given suggestion to a {@link Choice}. This will convert it to
     * a double or integer if necessary
     *
     * @param suggestion Suggestion to map
     * @param type       The option type
     * @return The choice
     */
    public static @NotNull Choice toChoice(@NotNull String suggestion, @NotNull OptionType type) {
        if (type == OptionType.INTEGER)
            return new Choice(suggestion, Integer.parseInt(suggestion));
        else if (type == OptionType.NUMBER)
            return new Choice(suggestion, Double.parseDouble(suggestion));
        else
            return new Choice(suggestion, suggestion);
    }

    /**
     * Maps the given list of suggestions to a list of {@link Choice}s. This will
     * convert them to doubles or integers if necessary.
     *
     * @param suggestions Suggestions to map
     * @param type        The suggestions type
     * @return The list of choices
     */
    public static @NotNull List<Choice> toChoices(@NotNull List<String> suggestions, @NotNull OptionType type) {
        return map(suggestions, s -> toChoice(s, type));
    }

    /**
     * Returns the required literal path of the given command. This will
     * join all the paths of literals until it encounters a non-literal.
     *
     * @param command Command to get the path for
     * @param <A>     The actor type
     * @return The path
     */
    @Contract(pure = true)
    public static <A extends CommandActor> @NotNull String getRequiredPath(@NotNull ExecutableCommand<A> command) {
        StringJoiner joiner = new StringJoiner(" ");
        for (CommandNode<A> node : command.nodes()) {
            if (node.isLiteral())
                joiner.add(node.name());
            else
                break;
        }
        return joiner.toString();
    }

    /**
     * Returns all the parameters in the given command.
     *
     * @param command The command to get parameters for
     * @param <A>     The actor type
     * @return The parameters, mapped by name
     */
    @Contract(pure = true)
    public static <A extends CommandActor> Map<String, ParameterNode<A, ?>> getParameters(
            @NotNull ExecutableCommand<A> command
    ) {
        Map<String, ParameterNode<A, ?>> nodes = new LinkedHashMap<>();
        for (CommandNode<A> node : command.nodes()) {
            if (node instanceof ParameterNode<A, ?> parameter) {
                nodes.put(node.name(), parameter);
            }
        }
        return nodes;
    }

    /**
     * Returns the {@link ChannelType} that corresponds to the given channel interface
     *
     * @param channelInterface The interface to check for
     * @return The channel type, or {@code null} if not found.
     */
    public static @Nullable ChannelType channelType(@NotNull Class<? extends Channel> channelInterface) {
        return CHANNEL_TYPES.get(channelInterface);
    }
}
