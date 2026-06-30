package revxrsal.commands.autocomplete;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.*;
import revxrsal.commands.stream.MutableStringStream;

import java.util.*;

import static revxrsal.commands.node.DispatcherSettings.LONG_FORMAT_PREFIX;
import static revxrsal.commands.node.DispatcherSettings.SHORT_FORMAT_PREFIX;

/**
 * Auto-completer for individual {@link ExecutableCommand ExecutableCommands}
 *
 * @param <A> Actor type
 */
final class SingleCommandCompleter<A extends CommandActor> {

    private final ExecutableCommand<A> command;
    private final MutableStringStream input;
    private final MutableExecutionContext<A> context;

    private final List<String> suggestions = new ArrayList<>();

    private int positionBeforeParsing = -1;

    public SingleCommandCompleter(A actor, ExecutableCommand<A> command, MutableStringStream input) {
        this.command = command;
        this.input = input;
        this.context = ExecutionContext.createMutable(command, actor, input.toImmutableView());
    }

    private void rememberPosition() {
        if (positionBeforeParsing != -1)
            throw new IllegalArgumentException("You already have a position remembered that you did not consume.");
        positionBeforeParsing = input.position();
    }

    private String restorePosition() {
        if (positionBeforeParsing == -1)
            throw new IllegalArgumentException("You forgot to call rememberPosition() when trying to restore position.");
        int positionAfterParsing = input.position();
        input.setPosition(positionBeforeParsing);
        positionBeforeParsing = -1;
        return input.peek(positionAfterParsing - positionBeforeParsing);
    }

    public void complete() {
        Map<String, ParameterNode<A, Object>> remainingFlags = null;
        for (CommandNode<A> node : command.nodes()) {
            if (node.isLiteral()) {
                CompletionResult result = completeLiteral(node.requireLiteralNode());
                if (result == CompletionResult.HALT)
                    break;
            } else {
                ParameterNode<A, Object> parameter = node.requireParameterNode();
                if (parameter.isFlag() || parameter.isSwitch()) {
                    (remainingFlags == null ? remainingFlags = new HashMap<>() : remainingFlags)
                            .put(universalFlagName(parameter), parameter);
                    continue;
                }
                CompletionResult result = completeParameter(parameter);
                if (result == CompletionResult.HALT)
                    break;
            }
        }
        if (!command.containsFlags() || remainingFlags == null)
            return;
        completeFlags(remainingFlags);
    }

    private CompletionResult completeParameter(@NotNull ParameterNode<A, Object> parameter) {
        rememberPosition();
        if (parameter.isSwitch()) {
            context.addResolvedArgument(parameter.name(), true);
            return CompletionResult.CONTINUE;
        }
        try {
            Object value = parameter.parse(input, context);
            context.addResolvedArgument(parameter.name(), value);
            int positionAfterParsing = input.position();
            String consumed = restorePosition();
            Collection<String> parameterSuggestions = parameter.complete(context);
            input.setPosition(positionAfterParsing); // restore so that we can move forward

            if (input.hasFinished()) {
                filterSuggestions(consumed, parameterSuggestions);
                return CompletionResult.HALT;
            }
            if (input.peek() == ' ')
                input.skipWhitespace();
            return CompletionResult.CONTINUE;
        } catch (Throwable t) {
            String consumed = restorePosition();
            filterSuggestions(consumed, parameter.complete(context));
            return CompletionResult.HALT;
        }
    }

    @Contract(mutates = "param1")
    private void completeFlags(@NotNull Map<String, ParameterNode<A, Object>> remainingFlags) {
        boolean lastWasShort = false;
        while (input.hasRemaining()) {
            if (input.peek() == ' ')
                input.skipWhitespace();
            if (input.hasFinished())
                break;
            String next = input.peekUnquotedString();
            if (next.isEmpty()) {
                input.moveForward();
            } else if (next.startsWith("--")) {
                lastWasShort = false;
                String flagName = next.substring(LONG_FORMAT_PREFIX.length());
                ParameterNode<A, Object> targetFlag = remainingFlags.remove(flagName);
                if (targetFlag == null) {
                    for (ParameterNode<A, Object> value : remainingFlags.values()) {
                        if (universalFlagName(value).startsWith(flagName))
                            suggestions.add(LONG_FORMAT_PREFIX + universalFlagName(value));
                    }
                    return;
                }
                input.readUnquotedString(); // consumes the flag name
                if (targetFlag.isSwitch()) {
                    context.addResolvedArgument(targetFlag.name(), true);
                    if (input.hasRemaining() && input.peek() == ' ')
                        input.skipWhitespace();
                    continue;
                }
                if (input.hasFinished())
                    return;
                if (input.remaining() == 1 && input.peek() == ' ') {
                    Collection<String> parameterSuggestions = targetFlag.complete(context);
                    suggestions.addAll(parameterSuggestions);
                    return;
                }
                input.skipWhitespace();
                CompletionResult result = completeParameter(targetFlag);
                if (result == CompletionResult.HALT) {
                    return;
                } else if (input.hasRemaining() && input.peek() == ' ') {
                    input.skipWhitespace();
                }
            } else if (next.startsWith("-")) {
                lastWasShort = true;
                String shortenedString = next.substring(SHORT_FORMAT_PREFIX.length());
                char[] spec = shortenedString.toCharArray();
                input.moveForward(SHORT_FORMAT_PREFIX.length());
                for (char flag : spec) {
                    input.moveForward();
                    @Nullable ParameterNode<A, Object> targetFlag = removeParameterWithShorthand(remainingFlags, flag);
                    if (targetFlag == null)
                        continue;
                    if (targetFlag.isSwitch()) {
                        context.addResolvedArgument(targetFlag.name(), true);
                    }
                    if (input.hasFinished()) {
                        if (targetFlag.isFlag())
                            return;
                        for (ParameterNode<A, Object> remFlag : remainingFlags.values()) {
                            if (remFlag.shorthand() != null) {
                                String flagCompletion = SHORT_FORMAT_PREFIX + shortenedString + remFlag.shorthand();
                                suggestions.add(remFlag.isFlag() ? flagCompletion + ' ' : flagCompletion);
                            }
                        }
                        return;
                    }
                    if (targetFlag.isSwitch())
                        continue;
                    if (input.remaining() == 1 && input.peek() == ' ') {
                        Collection<String> parameterSuggestions = targetFlag.complete(context);
                        suggestions.addAll(parameterSuggestions);
                        return;
                    }
                    if (input.hasRemaining() && input.peek() == ' ')
                        input.skipWhitespace();
                    CompletionResult result = completeParameter(targetFlag);
                    if (result == CompletionResult.HALT) {
                        return;
                    }

                }
            } else {
                input.moveForward(next.length());
            }
        }
        for (ParameterNode<A, Object> c : remainingFlags.values()) {
            if (lastWasShort)
                suggestions.add(SHORT_FORMAT_PREFIX + c.shorthand());
            else
                suggestions.add(LONG_FORMAT_PREFIX + (c.isSwitch() ? c.switchName() : c.flagName()));
        }
    }

    private @Nullable ParameterNode<A, Object> removeParameterWithShorthand(
            Map<String, ParameterNode<A, Object>> parametersLeft,
            char c
    ) {
        for (Iterator<Map.Entry<String, ParameterNode<A, Object>>> iterator = parametersLeft.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, ParameterNode<A, Object>> entry = iterator.next();
            Character shorthand = entry.getValue().shorthand();
            if (shorthand != null && shorthand == c) {
                iterator.remove();
                return entry.getValue();
            }
        }
        return null;
    }

    private CompletionResult completeLiteral(@NotNull LiteralNode<A> node) {
        String nextWord = input.readUnquotedString();
        if (input.hasFinished()) {
            if (node.name().startsWith(nextWord)) {
                // complete it for the user :)
                suggestions.add(node.name());
            }
            return CompletionResult.HALT;
        }
        if (!node.name().equalsIgnoreCase(nextWord)) {
            // the user inputted a command that isn't ours. dismiss the operation
            return CompletionResult.HALT;
        }
        if (input.hasRemaining() && input.peek() == ' ') {
            // our literal is just fine. move to the next node
            input.skipWhitespace();
            return CompletionResult.CONTINUE;
        }
        return CompletionResult.HALT;
    }

    private void filterSuggestions(String consumed, @NotNull Collection<String> parameterSuggestions) {
        for (String parameterSuggestion : parameterSuggestions) {
            if (parameterSuggestion.toLowerCase().startsWith(consumed.toLowerCase())) {
                suggestions.add(getRemainingContent(parameterSuggestion, consumed));
            }
        }
    }

    private String universalFlagName(@NotNull ParameterNode<A, Object> parameter) {
        if (parameter.isSwitch())
            return parameter.switchName();
        if (parameter.isFlag())
            return parameter.flagName();
        return parameter.name();
    }

    public @NotNull List<String> suggestions() {
        return suggestions;
    }

    /**
     * Represents the result of the completion of a {@link CommandNode}
     */
    private enum CompletionResult {

        /**
         * Halt the completion and don't return anything. This is sent when:
         * <ul>
         *     <li>
         *         When a node completes successfully
         *     </li>
         *     <li>
         *       The command being completed is not ours (i.e. user is completing "foo"
         *       but we're "bar")
         *     </li>
         *     <li>
         *         A node fails to complete because it cannot parse the given input
         *     </li>
         * </ul>
         */
        HALT,

        /**
         * Continue moving through the command nodes. This is sent when
         * all previous nodes have been successfully parsed, and the input
         * has been valid until now.
         */
        CONTINUE
    }

    private static String getRemainingContent(String suggestion, String consumed) {
        // Find the index where they match until
        int matchIndex = consumed.length();

        // Find the first space after the matching part
        int spaceIndex = suggestion.lastIndexOf(' ', matchIndex - 1);

        // Return the content after the first space
        return suggestion.substring(spaceIndex + 1);
    }

}
