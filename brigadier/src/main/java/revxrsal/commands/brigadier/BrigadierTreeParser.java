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
import revxrsal.commands.util.Primitives;

import java.util.ArrayList;
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

    /**
     * Parses all the registered commands and categories in the given {@link CommandHandler}
     * and registers all root trees and their corresponding children components
     * and parameters
     *
     * @param brigadier The platform's Brigadier implementation
     * @param handler   The command handler
     * @param namespace An optional namespace to register beside standard registration
     * @return All root nodes
     */
    public static <T> List<LiteralArgumentBuilder<T>> parse(
            @NotNull LampBrigadier brigadier,
            @NotNull CommandHandler handler,
            @Nullable String namespace) {
        List<LiteralArgumentBuilder<T>> nodes = new ArrayList<>();
        List<CommandCategory> roots = handler.getCategories().values().stream().filter(c -> c.getPath().size() == 1).collect(Collectors.toList());
        List<ExecutableCommand> rootCommands = handler.getCommands().values().stream().filter(c -> c.getPath().size() == 1).collect(Collectors.toList());
        for (CommandCategory root : roots) {
            nodes.add(parse(brigadier, literal(root.getName()), root));
            if (namespace != null) nodes.add(parse(brigadier, literal(namespace + ":" + root.getName()), root));
        }
        for (ExecutableCommand root : rootCommands) {
            nodes.add(parse(brigadier, literal(root.getName()), root));
            if (namespace != null) nodes.add(parse(brigadier, literal(namespace + ":" + root.getName()), root));
        }
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
        CommandNode<?> lastParameter = null;
        for (CommandParameter parameter : command.getValueParameters().values()) {
            CommandNode node = getBuilder(brigadier, command, parameter, true).build();
            if (lastParameter == null) {
                into.then(node);
            } else {
                lastParameter.addChild(node);
            }
            lastParameter = node;
        }
        return (LiteralArgumentBuilder<T>) into;
    }

    private static ArgumentBuilder getBuilder(LampBrigadier brigadier,
                                              ExecutableCommand command,
                                              CommandParameter parameter,
                                              boolean respectFlag) {
        if (parameter.isSwitch()) {
            return literal(parameter.getCommandHandler().getSwitchPrefix() + parameter.getSwitchName());
        }
        if (parameter.isFlag() && respectFlag) {
            return literal(parameter.getCommandHandler().getFlagPrefix() + parameter.getFlagName())
                    .then(getBuilder(brigadier, command, parameter, false));
        }
        ArgumentType<?> argumentType = getArgumentType(brigadier, parameter);

        return argument(parameter.getName(), argumentType)
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
                Message tooltip = new LiteralMessage(parameter.getName());
                String input = context.getInput();
                ArgumentStack args = ArgumentStack.forAutoCompletion(input.startsWith("/") ? input.substring(1) : input);
                parameter.getSuggestionProvider().getSuggestions(args, actor, command)
                        .stream()
                        .filter(c -> c.toLowerCase().startsWith(args.getLast().toLowerCase()))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .distinct()
                        .forEach(c -> builder.suggest(c, tooltip));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return builder.buildFuture();
        };
    }
}
