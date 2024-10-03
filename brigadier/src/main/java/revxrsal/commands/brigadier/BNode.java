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
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static revxrsal.commands.util.Preconditions.notNull;

final class BNode<S> {

    private final CommandNode<S> node;

    private BNode(CommandNode<S> node) {
        this.node = node;
    }

    public static <S> @NotNull BNode<S> of(@NotNull CommandNode<S> node) {
        notNull(node, "node");
        return new BNode<>(node);
    }

    public static <S> @NotNull BNode<S> literal(@NotNull String name) {
        return of(LiteralArgumentBuilder.<S>literal(name).build());
    }

    public @NotNull BNode<S> executes(Command<S> command) {
        Nodes.setCommand(node, command);
        return this;
    }

    public @NotNull BNode<S> requires(Predicate<S> requirement) {
        Nodes.setRequirement(node, requirement);
        return this;
    }

    public @NotNull BNode<S> suggests(SuggestionProvider<S> suggestionProvider) {
        if (node instanceof ArgumentCommandNode) {
            ArgumentCommandNode<S, ?> argument = (ArgumentCommandNode<S, ?>) node;
            Nodes.setSuggestionProvider(argument, suggestionProvider);
        }
        return this;
    }

    public @NotNull BNode<S> then(@NotNull BNode<S> node) {
        this.node.addChild(node.node);
        return this;
    }

    public @NotNull BNode<S> then(@NotNull CommandNode<S> node) {
        this.node.addChild(node);
        return this;
    }

    @Contract(pure = true)
    public @NotNull CommandNode<S> asBrigadierNode() {
        return node;
    }

    @NotNull BNode<S> nextChild() {
        return of(node.getChildren().iterator().next());
    }
}
