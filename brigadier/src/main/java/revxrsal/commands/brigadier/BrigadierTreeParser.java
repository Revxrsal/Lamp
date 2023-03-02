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
package revxrsal.commands.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.command.*;
import revxrsal.commands.exception.ArgumentParseException;
import revxrsal.commands.util.Primitives;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

/**
 * A utility class for parsing Lamp's components into Brigadier's.
 */
@SuppressWarnings("rawtypes")
public final class BrigadierTreeParser {

    private static final Command NO_ACTION = context -> Command.SINGLE_SUCCESS;

    /**
     * Parses all the registered commands and categories in the given {@link CommandHandler}
     * and registers all root trees and their corresponding children components
     * and parameters
     *
     * @param brigadier The platform's Brigadier implementation
     * @param handler   The command handler
     * @return All root nodes
     */
    public static <T> List<LiteralArgumentBuilder<T>> parse(
            @NotNull LampBrigadier brigadier,
            @NotNull CommandHandler handler
    ) {
        List<LiteralArgumentBuilder<T>> nodes = new ArrayList<>();
        List<CommandCategory> roots = handler.getCategories().values().stream().filter(c -> c.getPath().isRoot()).collect(Collectors.toList());
        List<ExecutableCommand> rootCommands = handler.getCommands().values().stream().filter(c -> c.getPath().isRoot()).collect(Collectors.toList());
        for (CommandCategory root : roots)
            nodes.add(parse(brigadier, literal(root.getName()), root));
        for (ExecutableCommand root : rootCommands)
            nodes.add(parse(brigadier, literal(root.getName()), root));
        return nodes;
    }

    /**
     * Parses the given command category into a {@link LiteralArgumentBuilder}.
     *
     * @param brigadier The platform's Brigadier implementation
     * @param into      The command node to register nodes into
     * @param category  Category to parse
     * @return The parsed command node
     */
    public static <T> LiteralArgumentBuilder<T> parse(LampBrigadier brigadier, LiteralArgumentBuilder<?> into, CommandCategory category) {
        for (CommandCategory child : category.getCategories().values()) {
            LiteralArgumentBuilder childLiteral = parse(brigadier, literal(child.getName()), child);
            into.then(childLiteral);
        }
        for (ExecutableCommand child : category.getCommands().values()) {
            LiteralArgumentBuilder childLiteral = parse(brigadier, literal(child.getName()), child);
            into.then(childLiteral);
        }
        if (category.getDefaultAction() != null) {
            parse(brigadier, into, category.getDefaultAction());
        }
        into.requires(a -> category.hasPermission(brigadier.wrapSource(a)));
        return (LiteralArgumentBuilder<T>) into;
    }

    /**
     * Parses the given command into a {@link LiteralArgumentBuilder}.
     *
     * @param brigadier The platform's Brigadier implementation
     * @param into      The command node to register nodes into
     * @param command   Command to parse
     * @return The parsed command node
     */
    public static <T> LiteralArgumentBuilder<T> parse(LampBrigadier brigadier,
                                                      LiteralArgumentBuilder<?> into,
                                                      ExecutableCommand command) {
        CommandNode lastParameter = null;
        List<CommandParameter> sortedParameters = new ArrayList<>(command.getValueParameters().values());
        Collections.sort(sortedParameters);
        for (int i = 0; i < sortedParameters.size(); i++) {
            boolean isLast = i == sortedParameters.size() - 1;
            CommandParameter parameter = sortedParameters.get(i);
            if (parameter.isFlag()) break;
            ArgumentBuilder<?, ?> builder = getBuilder(brigadier, command, parameter);
            if (!isLast && sortedParameters.get(i + 1).isOptional())
                builder.executes(NO_ACTION);
            if (lastParameter == null) {
                if (parameter.isOptional())
                    into.executes(NO_ACTION);
                into.then(lastParameter = builder.build());
            } else {
                lastParameter.addChild(lastParameter = builder.build());
            }
        }
        sortedParameters.removeIf(parameter -> !parameter.isFlag());
        CommandNode next = null;
        for (CommandParameter parameter : sortedParameters) {
            if (next == null) {
                if (lastParameter == null)
                    into.then(next = literal(parameter.getCommandHandler().getFlagPrefix() + parameter.getFlagName()).build());
                else
                    lastParameter.addChild(next = literal(parameter.getCommandHandler().getFlagPrefix() + parameter.getFlagName()).build());
            } else {
                next.addChild(next = literal(parameter.getCommandHandler().getFlagPrefix() + parameter.getFlagName()).build());
            }
            next.addChild(next = getBuilder(brigadier, command, parameter).build());
        }
        into.requires(a -> command.hasPermission(brigadier.wrapSource(a)));
        return (LiteralArgumentBuilder<T>) into;
    }

    private static ArgumentBuilder getBuilder(LampBrigadier brigadier,
                                              ExecutableCommand command,
                                              CommandParameter parameter) {
        if (parameter.isSwitch()) {
            return literal(parameter.getCommandHandler().getSwitchPrefix() + parameter.getSwitchName());
        }
        ArgumentType<?> argumentType = getArgumentType(brigadier, parameter);

        return argument(parameter.getName(), argumentType)
                .requires(a -> parameter.hasPermission(brigadier.wrapSource(a)))
                .suggests(createSuggestionProvider(brigadier, command, parameter));
    }

    private static ArgumentType<?> getArgumentType(LampBrigadier brigadier, @NotNull CommandParameter parameter) {
        ArgumentType<?> registeredType = brigadier.getArgumentType(parameter);
        if (registeredType != null) return registeredType;
        Class<?> type = Primitives.wrap(parameter.getType());
        registeredType = brigadier.getAdditionalArgumentTypes().getFlexible(type);
        if (registeredType != null)
            return registeredType;
        @Nullable Range range = parameter.getAnnotation(Range.class);
        if (type == String.class) {
            if (parameter.consumesAllString())
                return greedyString();
            return string();
        } else if (type == Integer.class) {
            if (range == null)
                return integer();
            return integer((int) range.min(), (int) range.max());
        } else if (type == Double.class) {
            if (range == null)
                return doubleArg();
            return doubleArg(range.min(), range.max());
        } else if (type == Float.class) {
            if (range == null)
                return floatArg();
            return floatArg((float) range.min(), (float) range.max());
        } else if (type == Long.class) {
            if (range == null)
                return longArg();
            return longArg((long) range.min(), (long) range.max());
        } else if (type == Boolean.class) {
            return bool();
        }
        return string();
    }

    private static SuggestionProvider<Object> createSuggestionProvider(
            LampBrigadier brigadier,
            ExecutableCommand command,
            CommandParameter parameter
    ) {
        if (parameter.getSuggestionProvider() == revxrsal.commands.autocomplete.SuggestionProvider.EMPTY)
            return null;
        return (context, builder) -> {
            try {
                CommandActor actor = brigadier.wrapSource(context.getSource());
                String tooltipMessage = parameter.getDescription() == null ? parameter.getName() : parameter.getDescription();
                Message tooltip = new LiteralMessage(tooltipMessage);
                String input = context.getInput();
                try {
                    ArgumentStack args = ArgumentStack.parseForAutoCompletion(
                            input.startsWith("/") ? input.substring(1) : input
                    );
                    parameter.getSuggestionProvider().getSuggestions(args, actor, command)
                            .stream()
                            .filter(c -> c.toLowerCase().startsWith(args.getLast().toLowerCase()))
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .distinct()
                            .forEach(c -> builder.suggest(c, tooltip));
                } catch (ArgumentParseException ignore) {}
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return builder.buildFuture();
        };
    }
}
