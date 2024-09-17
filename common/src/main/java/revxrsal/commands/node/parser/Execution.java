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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.CommandPriority;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.SecretCommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.command.*;
import revxrsal.commands.exception.ExpectedLiteralException;
import revxrsal.commands.exception.InputParseException;
import revxrsal.commands.exception.context.ErrorContext;
import revxrsal.commands.help.Help;
import revxrsal.commands.node.*;
import revxrsal.commands.stream.MutableStringStream;

import java.util.*;

import static java.util.Collections.unmodifiableMap;
import static revxrsal.commands.exception.context.ErrorContext.executingFunction;
import static revxrsal.commands.util.Collections.*;

final class Execution<A extends CommandActor> implements ExecutableCommand<A> {

    private final CommandFunction function;
    private final List<CommandNode<A>> nodes;
    private final @Unmodifiable Map<String, ParameterNode<A, Object>> parameters;
    private final CommandPermission<A> permission;
    private final int size;
    private final boolean isSecret;
    private final String description, usage;
    private final OptionalInt priority;
    private final String siblingPath;
    private final String path;
    private final boolean containsFlags;
    private int optionalParameters, requiredInput;
    private final boolean lowPriority;

    public Execution(CommandFunction function, List<CommandNode<A>> nodes) {
        this.function = function;
        this.nodes = nodes;
        this.parameters = computeParameters();
        this.size = nodes.size();
        //noinspection unchecked
        this.permission = (CommandPermission<A>) function.lamp().createPermission(function.annotations());
        for (CommandNode<A> node : nodes) {
            if (isOptional(node))
                optionalParameters++;
            else
                requiredInput++;
        }
        this.isSecret = function.annotations().contains(SecretCommand.class);
        this.description = function.annotations().map(Description.class, Description::value);
        this.path = computePath();
        this.usage = function.annotations().mapOrGet(Usage.class, Usage::value, this::path);
        this.priority = function.annotations()
                .mapOr(CommandPriority.class, c -> OptionalInt.of(c.value()), OptionalInt.empty());
        this.siblingPath = computeSiblingPath();
        this.containsFlags = any(nodes, n -> n instanceof ParameterNode<?, ?> p && (p.isFlag() || p.isSwitch()));
        this.lowPriority = function.annotations().contains(CommandPriority.Low.class);
        if (lowPriority && priority.isPresent()) {
            throw new IllegalArgumentException("You cannot have @CommandPriority and @CommandPriority.Low on the same function!");
        }
    }

    private static boolean isOptional(@NotNull CommandNode<? extends CommandActor> node) {
        return node instanceof ParameterNodeImpl && ((ParameterNode<? extends CommandActor, ?>) node).isOptional();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private @Unmodifiable Map<String, ParameterNode<A, Object>> computeParameters() {
        Map<String, ParameterNode<A, Object>> parameters = new LinkedHashMap<>();
        for (CommandNode<A> node : nodes) {
            if (node instanceof ParameterNode parameter)
                parameters.put(parameter.name(), parameter);
        }
        return unmodifiableMap(parameters);
    }

    private String computePath() {
        StringJoiner joiner = new StringJoiner(" ");
        for (CommandNode<A> n : nodes) {
            joiner.add(n.representation());
        }
        return joiner.toString();
    }

    private @NotNull String computeSiblingPath() {
        StringJoiner joiner = new StringJoiner(" ");
        int index = 0;
        for (int i = nodes.size() - 1; i >= 0; i--) {
            CommandNode<A> n = nodes.get(i);
            if (n.isLiteral()) {
                index = i;
                break;
            }
        }
        for (int i = 0; i < index; i++) {
            CommandNode<A> n = nodes.get(i);
            joiner.add(n.representation());
        }
        return joiner.toString();
    }

    @Override
    public @NotNull Lamp<A> lamp() {
        return function.lamp();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int optionalParameters() {
        return optionalParameters;
    }

    @Override
    public int requiredInput() {
        return requiredInput;
    }

    @Override
    public @NotNull String path() {
        return path;
    }

    @Override
    public @NotNull String usage() {
        return usage;
    }

    @Override
    public @NotNull CommandPermission<A> permission() {
        return permission;
    }

    @Override
    public @NotNull CommandFunction function() {
        return function;
    }

    @Override
    public @NotNull CommandNode<A> lastNode() {
        return nodes.get(nodes.size() - 1);
    }

    @Override
    public @NotNull LiteralNodeImpl<A> firstNode() {
        return ((LiteralNodeImpl<A>) nodes.get(0));
    }

    @Override
    public @NotNull Potential<A> test(@NotNull A actor, @NotNull MutableStringStream input) {
        return new ParseResult<>(this, actor, input);
    }

    @Override
    public void unregister() {
        lamp().unregister(this);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override public void execute(@NotNull ExecutionContext<A> context) {
        try {
            for (var condition : context.lamp().commandConditions())
                condition.test((ExecutionContext) context);
            action().execute(context);
        } catch (Throwable t) {
            lamp().handleException(t, executingFunction(context));
        }
    }

    @Override public Help.@NotNull RelatedCommands<A> relatedCommands(@Nullable A filterFor) {
        return new HelpImpl.RelatedCommandsImpl<>(
                filter(lamp().registry().commands(), command -> {
                    return command != this
                            && !command.isSecret()
                            && isRelatedTo(command)
                            && (filterFor == null || command.permission().isExecutableBy(filterFor)
                    );
                })
        );
    }

    @Override public Help.@NotNull ChildrenCommands<A> childrenCommands(@Nullable A filterFor) {
        return new HelpImpl.ChildrenCommandsImpl<>(
                filter(lamp().registry().commands(), command -> {
                    return command != this
                            && !command.isSecret()
                            && isParentOf(command)
                            && (filterFor == null || command.permission().isExecutableBy(filterFor)
                    );
                })
        );
    }

    @Override public Help.@NotNull SiblingCommands<A> siblingCommands(@Nullable A filterFor) {
        return new HelpImpl.SiblingCommandsImpl<>(
                filter(lamp().registry().commands(), command -> {
                    return command != this
                            && !command.isSecret()
                            && isSiblingOf(command)
                            && (filterFor == null || command.permission().isExecutableBy(filterFor)
                    );
                })
        );
    }

    @Override public @NotNull @Unmodifiable Map<String, ParameterNode<A, Object>> parameters() {
        return parameters;
    }

    @Override public boolean isSiblingOf(@NotNull ExecutableCommand<A> command) {
        return siblingPath.equalsIgnoreCase(((Execution<A>) command).siblingPath);
    }

    @Override public boolean isChildOf(@NotNull ExecutableCommand<A> command) {
        return path().startsWith(command.path());
    }

    @Override public boolean containsFlags() {
        return containsFlags;
    }

    @Override
    public boolean isSecret() {
        return isSecret;
    }

    @Override
    public String toString() {
        return "ExecutableCommand(path='" + path() + "')";
    }

    @Override
    public @Nullable String description() {
        return description;
    }

    @Override
    public @NotNull @Unmodifiable List<CommandNode<A>> nodes() {
        return Collections.unmodifiableList(nodes);
    }

    @Override public @NotNull OptionalInt commandPriority() {
        return priority;
    }

    @Override
    public int compareTo(@NotNull ExecutableCommand<A> o) {
        if (!(o instanceof Execution<A> exec)) {
            return 0; // Handle the case where o is not an instance of Execution
        }
        if (lowPriority != exec.lowPriority) {
            return lowPriority ? 1 : -1;
        }
        // Compare by priority if both have priorities
        if (commandPriority().isPresent() && o.commandPriority().isPresent()) {
            return Integer.compare(commandPriority().getAsInt(), o.commandPriority().getAsInt());
        }

        // Compare by size if priorities are not present
        int sizeComparison = Integer.compare(size, exec.size);
        if (sizeComparison != 0) {
            return sizeComparison; // Higher size = higher priority
        }

        // Compare optional status of the last node if sizes are equal
        if (isOptional(lastNode()) != isOptional(o.lastNode())) {
            return isOptional(lastNode()) ? 1 : -1;
        }

        // Compare the last node if everything else is equal
        return lastNode().compareTo(o.lastNode());
    }

    @Override
    public @NotNull Iterator<CommandNode<A>> iterator() {
        return unmodifiableIterator(nodes.iterator());
    }

    static final class ParseResult<A extends CommandActor> implements Potential<A> {
        private final Execution<A> execution;
        private final MutableExecutionContext<A> context;
        private final boolean testResult;
        private MutableStringStream input;
        private boolean consumedAllInput = false;
        private @Nullable Throwable error;
        private @Nullable ErrorContext<A> errorContext;

        public ParseResult(Execution<A> execution, A actor, MutableStringStream input) {
            this.execution = execution;
            this.context = ExecutionContext.createMutable(execution, actor, input.toImmutableCopy());
            this.input = input;
            this.testResult = test();
        }

        private boolean test() {
            if (execution.containsFlags) {
                MutableStringStream original = input.toMutableCopy();
                if (!tryParseFlags()) {
                    input = original;
                    return false;
                }
            }
            for (CommandNode<A> node : execution.nodes) {
                if (node instanceof ParameterNode<?, ?> p && (p.isFlag() || p.isSwitch()))
                    continue;
                if (!tryParse(node, input, context)) {
                    context.clearResolvedArguments();
                    return false;
                }
            }
            if (!testConditions()) {
                return false;
            }
            consumedAllInput = input.hasFinished();
            return true;
        }

        private boolean tryParseFlags() {
            FlagParser<A> flagParser = new FlagParser<>(context, input);
            if (!flagParser.tryParse()) {
                error = flagParser.error();
                errorContext = flagParser.errorContext();
                context.clearResolvedArguments();
                return false;
            }
            input = flagParser.strippedInput();
            return true;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private boolean testConditions() {
            try {
                for (var condition : context.lamp().commandConditions()) {
                    condition.test(((ExecutionContext) context));
                }
                return true;
            } catch (Throwable t) {
                error = t;
                errorContext = executingFunction(context);
                return false;
            }
        }

        @Override
        public boolean successful() {
            return testResult;
        }

        @Override
        public @NotNull ExecutionContext<A> context() {
            return context;
        }

        @Override
        public boolean failed() {
            return !testResult;
        }

        @Override
        public void handleException() {
            if (error != null && errorContext != null)
                context().lamp().handleException(error, errorContext);
        }

        @Override
        public @Nullable Throwable error() {
            return error;
        }

        @Override
        public @Nullable ErrorContext<A> errorContext() {
            return errorContext;
        }

        @Override
        public void execute() {
            if (error == null) {
                if (execution.lamp().hooks().onCommandExecuted(execution, context))
                    execution.lastNode().execute(context, input);
            }
        }

        @Override
        public int compareTo(@NotNull Potential<A> o) {
            if (o.getClass() != getClass())
                return 0;
            ParseResult<A> result = ((ParseResult<A>) o);
            if (consumedAllInput != result.consumedAllInput)
                return consumedAllInput ? -1 : 1;
            return execution.compareTo(result.execution);
        }

        @SuppressWarnings("unchecked")
        private boolean tryParse(
                CommandNode<A> node,
                MutableStringStream input,
                MutableExecutionContext<A> context
        ) {
            if (input.hasRemaining() && input.peek() == ' ')
                input.moveForward();
            int pos = input.position();
            if (node instanceof LiteralNodeImpl<A> l) {
                String value = input.readUnquotedString();
                if (node.name().equalsIgnoreCase(value)) {
                    checkForSpace(input);
                    return true;
                }
                input.setPosition(pos);
                error = new ExpectedLiteralException(value, (LiteralNode<CommandActor>) l);
                errorContext = ErrorContext.parsingLiteral(context, l);
                return false;
            }
            ParameterNodeImpl<A, Object> parameter = (ParameterNodeImpl<A, Object>) node;
            try {
                Object value = parameter.parse(input, context);
                Lamp<A> lamp = execution.function().lamp();
                context.addResolvedArgument(parameter.name(), value);
                checkForSpace(input);
                return true;
            } catch (Throwable t) {
                input.setPosition(pos);
                error = t;
                errorContext = ErrorContext.parsingParameter(context, parameter, input);
                return false;
            }
        }

        private void checkForSpace(MutableStringStream input) {
            if (input.hasRemaining() && input.peek() != ' ')
                throw new InputParseException(InputParseException.Cause.EXPECTED_WHITESPACE);
        }

        @Override
        public String toString() {
            if (successful())
                return "Potential(path=" + execution.path() + ", success=true)";
            else
                return "Potential(path=" + execution.path() + ", success=false, error=" + error + ")";
        }
    }
}
