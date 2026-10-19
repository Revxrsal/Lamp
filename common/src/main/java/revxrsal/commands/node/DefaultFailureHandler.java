package revxrsal.commands.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.Potential;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.ExpectedLiteralException;
import revxrsal.commands.exception.UnknownCommandException;
import revxrsal.commands.exception.context.ErrorContext;
import revxrsal.commands.stream.StringStream;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static revxrsal.commands.util.Collections.filter;
import static revxrsal.commands.util.Strings.closestMatches;

/**
 * The default failure handler. This can be overridden in {@link DispatcherSettings}
 * from {@link Lamp.Builder#dispatcherSettings()}.
 * <p>
 * This reports the error of the attempt that got furthest into the input. If
 * every such attempt expected a different subcommand, it suggests the ones
 * closest to what the user typed.
 *
 * @param <A> The actor type
 */
final class DefaultFailureHandler<A extends CommandActor> implements FailureHandler<A> {

    private static final int MAX_NUMBER_OF_SUGGESTIONS = 6;
    private static final DefaultFailureHandler<CommandActor> INSTANCE = new DefaultFailureHandler<>();

    @SuppressWarnings("unchecked")
    public static <A extends CommandActor> FailureHandler<A> defaultFailureHandler() {
        return (FailureHandler<A>) INSTANCE;
    }

    @Override
    public void handleFailedAttempts(@NotNull A actor, @NotNull @Unmodifiable List<Potential<A>> failedAttempts, @NotNull StringStream input) {
        if (failedAttempts.isEmpty()) {
            return;
        }
        // The attempts that got furthest into the input carry the most relevant errors
        int furthest = 0;
        for (Potential<A> attempt : failedAttempts)
            furthest = Math.max(furthest, attempt.failurePosition());
        int furthestPosition = furthest;
        List<Potential<A>> furthestAttempts = filter(failedAttempts, v -> v.failurePosition() == furthestPosition);

        List<Potential<A>> realExceptions = filter(furthestAttempts, v -> !(v.error() instanceof ExpectedLiteralException));
        if (!realExceptions.isEmpty()) {
            realExceptions.get(0).handleException();
            return;
        }

        // All attempts expected some literal here. Only suggest the ones the actor can see.
        String found = ((ExpectedLiteralException) furthestAttempts.get(0).error()).input();
        List<Potential<A>> visible = filter(furthestAttempts, v -> isVisibleTo(v.context().command(), actor));
        if (visible.isEmpty()) {
            Lamp<A> lamp = furthestAttempts.get(0).context().lamp();
            lamp.handleException(new UnknownCommandException(found), ErrorContext.unknownCommand(actor));
            return;
        }
        if (visible.size() == 1) {
            visible.get(0).handleException();
            return;
        }

        Map<String, ExecutableCommand<A>> byLiteral = new LinkedHashMap<>();
        for (Potential<A> attempt : visible) {
            String literal = ((ExpectedLiteralException) attempt.error()).node().name();
            byLiteral.putIfAbsent(literal, attempt.context().command());
        }
        List<String> closest = closestMatches(found, byLiteral.keySet(), MAX_NUMBER_OF_SUGGESTIONS);
        Collection<String> suggested = closest.isEmpty() ? byLiteral.keySet() : closest;

        actor.error("Failed to find a suitable command for your input (\"" + input.source() + "\"). Did you mean:");
        int count = 0;
        for (String literal : suggested) {
            if (count++ >= MAX_NUMBER_OF_SUGGESTIONS)
                break;
            actor.reply("- " + byLiteral.get(literal).path());
        }
    }

    private boolean isVisibleTo(@NotNull ExecutableCommand<A> command, @NotNull A actor) {
        return !command.isSecret() && command.permission().isExecutableBy(actor);
    }
}
