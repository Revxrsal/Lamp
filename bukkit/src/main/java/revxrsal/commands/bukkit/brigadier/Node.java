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

import static revxrsal.commands.util.Preconditions.notNull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitBrigadier;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;

/**
 * Represents a node in the Brigadier tree. Since Brigadier only allows modifying elements inside
 * builders, this one captures already-built instances and modifies it safely using reflection to
 * avoid bugs from immutability.
 */
@SuppressWarnings("rawtypes")
final class Node {

  private final CommandNode<?> node;

  public Node(ArgumentBuilder<?, ?> node) {
    this.node = node.build();
  }

  public Node(CommandNode node) {
    this.node = node;
  }

  public void addChild(@NotNull Node node) {
    notNull(node, "node");
    this.node.addChild((CommandNode) node.node);
  }

  public void addChildren(@NotNull List<Node> nodes) {
    notNull(nodes, "nodes");
    for (Node node : nodes) {
      this.node.addChild((CommandNode) node.node);
    }
  }

  public void action(Command<?> command) {
    NodeReflection.setCommand(node, (Command) command);
  }

  public void canBeExecuted(BukkitBrigadier brigadier) {
    action(a -> {
      String input = a.getInput();
      ArgumentStack args = brigadier.getCommandHandler().parseArgumentsForCompletion(
          input.startsWith("/") ? input.substring(1) : input
      );
      CommandActor actor = brigadier.wrapSource(a.getSource());
      try {
        brigadier.getCommandHandler().dispatch(actor, args);
      } catch (Throwable t) {
        brigadier.getCommandHandler().getExceptionHandler().handleException(t, actor);
      }
      return Command.SINGLE_SUCCESS;
    });
  }

  public void require(Predicate<Object> requirement) {
    NodeReflection.setRequirement(node, (Predicate) requirement);
  }

  public void suggest(SuggestionProvider provider) {
    if (!(node instanceof ArgumentCommandNode)) {
      throw new IllegalArgumentException("Not an argument node.");
    }
    NodeReflection.setSuggestionProvider(((ArgumentCommandNode) node), provider);
  }

  public <T extends CommandNode<?>> T getNode() {
    return (T) node;
  }
}
