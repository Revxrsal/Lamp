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
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.SecretCommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.command.*;
import revxrsal.commands.exception.ExpectedLiteralException;
import revxrsal.commands.exception.InputParseException;
import revxrsal.commands.exception.TrailingInputException;
import revxrsal.commands.exception.context.ErrorContext;
import revxrsal.commands.help.Help;
import revxrsal.commands.node.*;
import revxrsal.commands.process.CommandCondition;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

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
    private final String[] siblingPath;
    private final String path;
    private final int flagCount;
    private final List<CommandNode<A>> positionalNodes;
    private int optionalParameters, requiredInput;

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
        this.siblingPath = computeSiblingPath();
        this.flagCount = count(nodes, Execution::isFlagOrSwitch);
        this.positionalNodes = filter(nodes, n -> !isFlagOrSwitch(n));
    }

    private static boolean isOptional(@NotNull CommandNode<? extends CommandActor> node) {
        return node instanceof ParameterNodeImpl && ((ParameterNode<? extends CommandActor, ?>) node).isOptional();
    }

    private static boolean isFlagOrSwitch(@NotNull CommandNode<? extends CommandActor> node) {
        return node instanceof ParameterNode<?, ?> && (((ParameterNode<?, ?>) node).isFlag() || ((ParameterNode<?, ?>) node).isSwitch());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private @Unmodifiable Map<String, ParameterNode<A, Object>> computeParameters() {
        Map<String, ParameterNode<A, Object>> parameters = new LinkedHashMap<>();
        for (CommandNode<A> node : nodes) {
            if (node instanceof ParameterNode) {
                ParameterNode parameter = (ParameterNode) node;
                parameters.put(parameter.name(), parameter);
            }
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

    private @NotNull String[] computeSiblingPath() {
        List<String> path = new ArrayList<>();
        int index = 0;
        for (int i = nodes.size() - 1; i >= 0; i--) {
            CommandNode<A> n = nodes.get(i);
            if (n.isLiteral()) {
                index = i;
                break;
            }
        }
        if (index == 0) {
            // This is a top-level command: the sibling path should be the
            // actual command label.
            CommandNode<A> n = nodes.get(0);
            if (!n.isLiteral())
                throw new IllegalArgumentException("The command does not start with a literal.");
            path.add(n.representation());
        } else {
            for (int i = 0; i < index; i++) {
                CommandNode<A> n = nodes.get(i);
                path.add(n.representation());
            }
        }
        return path.toArray(new String[0]);
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
            for (CommandCondition<? super A> condition : context.lamp().commandConditions())
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
        if (command == this)
            return false;

        String[] otherPath = ((Execution<A>) command).siblingPath;
        return Arrays.equals(siblingPath, otherPath);
    }

    @Override public boolean isRelatedTo(@NotNull ExecutableCommand<A> command) {
        if (command == this)
            return false;
        return isParentOf(command) || isSiblingOf(command) || isNestedUnderSiblingPath(command);
    }

    /**
     * Tests whether the provided command lives below this command's level, i.e. it is
     * a subcommand of one of this command's siblings (at any depth).
     * <p>
     * For example, {@code foo bar baz <x>} is nested under the sibling path of {@code foo help}.
     */
    private boolean isNestedUnderSiblingPath(@NotNull ExecutableCommand<A> command) {
        String[] otherPath = ((Execution<A>) command).siblingPath;
        if (otherPath.length <= siblingPath.length)
            return false;
        for (int i = 0; i < siblingPath.length; i++) {
            if (!siblingPath[i].equals(otherPath[i]))
                return false;
        }
        return true;
    }

    @Override public boolean isChildOf(@NotNull ExecutableCommand<A> command) {
        if (size() <= command.size())
            return false;
        for (int i = 0; i < command.size(); i++) {
            CommandNode<A> ourNode = nodes.get(i);
            CommandNode<A> otherNode = command.nodes().get(i);
            if (!otherNode.representation().equals(ourNode.representation()))
                return false;
        }
        return true;
    }

    @Override public boolean containsFlags() {
        return flagCount > 0;
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

    @Override public @Range(from = 0, to = Integer.MAX_VALUE) int flagCount() {
        return flagCount;
    }

    @Override
    public @NotNull @Unmodifiable List<CommandNode<A>> nodes() {
        return Collections.unmodifiableList(nodes);
    }

    @Override
    public int compareTo(@NotNull ExecutableCommand<A> o) {
        if (!(o instanceof Execution)) {
            return 0; // Handle the case where o is not an instance of Execution
        }
        Execution<A> exec = (Execution<A>) o;
        // Compare the positional nodes from left to right. The first position at which
        // the commands differ decides: literals before parameters, required parameters
        // before optional ones, then the parameter types' PrioritySpec.
        int shared = Math.min(positionalNodes.size(), exec.positionalNodes.size());
        for (int i = 0; i < shared; i++) {
            int nodeComparison = positionalNodes.get(i).compareTo(exec.positionalNodes.get(i));
            if (nodeComparison != 0)
                return nodeComparison;
        }

        // One command is a prefix of the other: the shorter one comes first
        return Integer.compare(positionalNodes.size(), exec.positionalNodes.size());
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
        private int failurePosition = -1;

        public ParseResult(Execution<A> execution, A actor, MutableStringStream input) {
            this.execution = execution;
            this.context = ExecutionContext.createMutable(execution, actor, input.toImmutableCopy());
            this.input = input;
            this.testResult = test();
        }

        /**
         * Creates a failed result for a command whose literal at {@code literalIndex}
         * does not match {@code found}, without parsing anything. This is what
         * testing the command would produce, as everything before that literal
         * is literals as well.
         */
        @SuppressWarnings("unchecked")
        ParseResult(Execution<A> execution, A actor, StringStream input, int literalIndex, String found, int position) {
            LiteralNodeImpl<A> expected = (LiteralNodeImpl<A>) execution.nodes.get(literalIndex);
            this.execution = execution;
            this.context = ExecutionContext.createMutable(execution, actor, input.toImmutableCopy());
            this.input = input.toMutableCopy();
            this.testResult = false;
            this.error = new ExpectedLiteralException(found, (LiteralNode<CommandActor>) (LiteralNode<?>) expected);
            this.errorContext = ErrorContext.parsingLiteral(context, expected);
            this.failurePosition = position;
        }

        private boolean test() {
            if (execution.flagCount > 0) {
                MutableStringStream original = input.toMutableCopy();
                if (!tryParseFlags()) {
                    input = original;
                    return false;
                }
            }
            // On failure, the context keeps the arguments parsed so far. Suggestion
            // providers and error handlers may need them.
            for (CommandNode<A> node : execution.positionalNodes) {
                if (!tryParse(node, input, context))
                    return false;
            }
            if (input.hasRemaining() && input.peek() == ' ')
                input.skipWhitespace();
            consumedAllInput = input.hasFinished();
            if (!consumedAllInput && execution.lamp().dispatcherSettings().rejectTrailingInput()) {
                fail(new TrailingInputException(input.peekRemaining()), executingFunction(context), input.position());
                return false;
            }
            return testPermission();
        }

        private boolean tryParseFlags() {
            FlagParser<A> flagParser = new FlagParser<>(context, input);
            if (!flagParser.tryParse()) {
                fail(flagParser.error(), flagParser.errorContext(), input.position());
                return false;
            }
            input = flagParser.strippedInput();
            return true;
        }

        private void fail(Throwable error, ErrorContext<A> errorContext, int position) {
            this.error = error;
            this.errorContext = errorContext;
            this.failurePosition = position;
        }

        /**
         * Permissions take part in choosing a command, so an actor that lacks the
         * permission for one overload can still run another. Other conditions only
         * run on the chosen command (see {@link #execute()}).
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        private boolean testPermission() {
            if (!context.lamp().commandConditions().contains(PermissionConditionChecker.INSTANCE))
                return true;
            try {
                PermissionConditionChecker.INSTANCE.test((ExecutionContext) context);
                return true;
            } catch (Throwable t) {
                fail(t, executingFunction(context), input.position());
                return false;
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private boolean testConditions() {
            try {
                for (CommandCondition<? super A> condition : context.lamp().commandConditions()) {
                    condition.test(((ExecutionContext) context));
                }
                return true;
            } catch (Throwable t) {
                fail(t, executingFunction(context), input.position());
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
        public int failurePosition() {
            return testResult ? -1 : failurePosition;
        }

        @Override
        public boolean consumedAllInput() {
            return testResult && consumedAllInput;
        }

        @Override
        public void execute() {
            if (!testResult || error != null)
                return;
            if (!testConditions()) {
                handleException();
                return;
            }
            if (execution.lamp().hooks().onCommandExecuted(execution, context))
                execution.lastNode().execute(context, input);
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
                input.skipWhitespace();
            int pos = input.position();
            if (node instanceof LiteralNodeImpl) {
                LiteralNodeImpl<A> l = (LiteralNodeImpl<A>) node;
                String value = input.readUnquotedString();
                if (!node.name().equalsIgnoreCase(value)) {
                    input.setPosition(pos);
                    fail(new ExpectedLiteralException(value, (LiteralNode<CommandActor>) l), ErrorContext.parsingLiteral(context, l), pos);
                    return false;
                }
                if (input.hasRemaining() && input.peek() != ' ') {
                    input.setPosition(pos);
                    fail(new InputParseException(InputParseException.Cause.EXPECTED_WHITESPACE), ErrorContext.parsingLiteral(context, l), pos);
                    return false;
                }
                return true;
            }
            ParameterNodeImpl<A, Object> parameter = (ParameterNodeImpl<A, Object>) node;
            try {
                Object value = parameter.parse(input, context);
                context.addResolvedArgument(parameter.name(), value);
                checkForSpace(input);
                return true;
            } catch (Throwable t) {
                input.setPosition(pos);
                fail(t, ErrorContext.parsingParameter(context, parameter, input), pos);
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
