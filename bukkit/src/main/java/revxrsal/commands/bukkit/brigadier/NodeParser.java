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
package revxrsal.commands.bukkit.brigadier;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.BukkitBrigadier;
import revxrsal.commands.bukkit.core.BukkitHandler;
import revxrsal.commands.command.*;
import revxrsal.commands.command.trait.PermissionHolder;
import revxrsal.commands.core.EitherParameter;
import revxrsal.commands.exception.ArgumentParseException;
import revxrsal.commands.util.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static java.util.Collections.singletonList;
import static revxrsal.commands.autocomplete.SuggestionProvider.EMPTY;
import static revxrsal.commands.util.Collections.listOf;

final class NodeParser {

    private final BukkitBrigadier brigadier;

    public NodeParser(BukkitBrigadier brigadier) {
        this.brigadier = brigadier;
    }

    private Node createNode(ArgumentBuilder<?, ?> builder) {
//        return Node.from(builder).canBeExecuted(brigadier);
        return Node.from(builder);
    }

    public List<Node> parse(CommandHandler handler) {
        List<Node> nodes = new ArrayList<>();
        for (CommandCategory category : handler.getCategories().values()) {
            if (category.getPath().isRoot()) nodes.add(create(category));
        }
        for (ExecutableCommand command : handler.getCommands().values()) {
            if (command.getPath().isRoot()) nodes.add(create(command));
        }
        return nodes;
    }

    private List<Node> createNodes(CommandParameter parameter) {
        if (parameter.isSwitch()) {
            String switchLiteral = parameter.getCommandHandler().getSwitchPrefix() + parameter.getSwitchName();
            return singletonList(createNode(literal(switchLiteral)));
        }
        if (parameter.getType() == Either.class) {
            EitherParameter[] either = EitherParameter.create(parameter);
            List<Node> first = createNodes(either[0]);
            List<Node> second = createNodes(either[1]);
            return Stream.concat(first.stream(), second.stream()).collect(Collectors.toList());
        }
        ExecutableCommand command = parameter.getDeclaringCommand();

        ArgumentType<?> argumentType = brigadier.getArgumentType(parameter);
        boolean isLast = parameter.getCommandIndex() == command.getValueParameters().size() - 1;

        Node node = createNode(argument(parameter.getName(), argumentType));
        node.require(generateRequirement(parameter));
        node.suggest(createSuggestionProvider(brigadier, parameter));
        if (isLast)
            node.canBeExecuted(brigadier);
        return singletonList(node);
    }

    public Node create(CommandCategory category) {
        Node node = createNode(literal(category.getName()));
        node.require(generateRequirement(category));

        for (CommandCategory subcategory : category.getCategories().values()) {
            Node subNode = create(subcategory);
            node.addChild(subNode);
        }

        for (ExecutableCommand subcommands : category.getCommands().values()) {
            Node subNode = create(subcommands);
            node.addChild(subNode);
        }

        if (category.getDefaultAction() != null)
            addExecutables(category.getDefaultAction(), node);

        return node;
    }

    private Predicate<Object> generateRequirement(PermissionHolder holder) {
        return sender -> holder.getPermission().canExecute(brigadier.wrapSource(sender));
    }

    public Node create(ExecutableCommand command) {
        Node node = createNode(literal(command.getName()));
        node.require(generateRequirement(command));
        addExecutables(command, node);
        return node;
    }

    public void addExecutables(ExecutableCommand command, Node targetNode) {
        if (command.getValueParameters().isEmpty()) {
            targetNode.canBeExecuted(brigadier);
            return;
        }

        addParameterNodes(command, targetNode);
    }

    private void addParameterNodes(ExecutableCommand command, Node targetNode) {
        ArrayList<Node> lastNodes = (ArrayList<Node>) listOf(targetNode);

        List<CommandParameter> parameters = new ArrayList<>(command.getValueParameters().values());

        for (CommandParameter parameter : parameters) {
            if (parameter.isFlag()) {
                addFlagParameter(parameter, lastNodes);
                continue;
            }
            if (parameter.isOptional() || parameter.isSwitch())
                lastNodes.forEach(lastNode -> lastNode.canBeExecuted(brigadier));

            List<Node> paramNodes = createNodes(parameter);
            if (paramNodes == null || paramNodes.isEmpty()) continue;
            lastNodes.forEach(lastNode -> lastNode.addChildren(paramNodes));

            lastNodes.clear();
            lastNodes.addAll(paramNodes);
        }
    }

    private void addFlagParameter(CommandParameter parameter, ArrayList<Node> lastNodes) {
        Node flagLiteral = createNode(literal(parameter.getCommandHandler().getFlagPrefix() + parameter.getFlagName()));
        flagLiteral.require(generateRequirement(parameter));

        if (parameter.isOptional())
            lastNodes.forEach(lastNode -> lastNode.canBeExecuted(brigadier));

        List<Node> flagNodes = createNodes(parameter);
        flagLiteral.addChildren(flagNodes);

        lastNodes.forEach(lastNode -> lastNode.addChild(flagLiteral));
        lastNodes.clear();

        lastNodes.addAll(flagNodes);
    }

    private static SuggestionProvider<Object> createSuggestionProvider(
            BukkitBrigadier brigadier,
            CommandParameter parameter
    ) {
        if (parameter.getSuggestionProvider() == EMPTY)
            return null;
        if (brigadier.isNativePlayerCompletionEnabled() &&
                parameter.getSuggestionProvider() == BukkitHandler.playerSuggestionProvider)
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
                    parameter.getSuggestionProvider().getSuggestions(args, actor, parameter.getDeclaringCommand())
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
