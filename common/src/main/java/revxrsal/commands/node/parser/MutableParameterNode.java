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
package revxrsal.commands.node.parser;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.node.CommandNode;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.parameter.ParameterType;

@Setter
final class MutableParameterNode<A extends CommandActor, T> extends MutableCommandNode<A> implements Comparable<MutableParameterNode<A, Object>> {

    private @NotNull ParameterType<A, T> type;
    private @NotNull SuggestionProvider<A> suggestions = SuggestionProvider.empty();
    private @NotNull CommandPermission<A> permission = CommandPermission.alwaysTrue();

    private @NotNull CommandParameter parameter;
    private boolean isOptional;

    public MutableParameterNode(@NotNull String name) {
        super(name);
    }

    @Override
    public int compareTo(@NotNull MutableParameterNode<A, Object> o) {
        return type.parsePriority().comparator().compare(type(), o.type());
    }

    public @NotNull ParameterNode<A, T> createNode() {
        return new ParameterNodeImpl<>(
                getName(),
                getAction(),
                isLast(),
                type,
                suggestions,
                parameter,
                permission,
                isOptional
        );
    }

    @Override
    public CommandNode<A> toNode() {
        return createNode();
    }

    public @NotNull ParameterType<A, T> type() {
        return this.type;
    }

    public @NotNull SuggestionProvider<A> suggestions() {
        return this.suggestions;
    }

    public @NotNull CommandParameter parameter() {
        return this.parameter;
    }

    public boolean isOptional() {
        return this.isOptional;
    }
}
