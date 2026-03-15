package revxrsal.commands.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.command.Potential;
import revxrsal.commands.exception.ExpectedLiteralException;
import revxrsal.commands.exception.UnknownCommandException;
import revxrsal.commands.exception.context.ErrorContext;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.stream.StringStream;

import java.util.ArrayList;
import java.util.List;

import static revxrsal.commands.util.Collections.filter;

/**
 * The default failure handler. This can be overridden in {@link DispatcherSettings}
 * from {@link Lamp.Builder#dispatcherSettings()}.
 * <p>
 * This will simply tell the user
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

        List<Potential<A>> visibleAttempts = filter(failedAttempts, attempt -> attempt.context().command().isVisibleTo(actor));
        for (Potential<A> potential : failedAttempts) {
            if (!potential.context().command().permission().isExecutableBy(actor)
                    && !(potential.error() instanceof ExpectedLiteralException)) {
                handleNoPermission(potential.context());
                return;
            }
        }

        if (visibleAttempts.size() > 1) {
            List<String> suggestions = new ArrayList<>(visibleAttempts.size());
            for (Potential<A> attempt : visibleAttempts) {
                suggestions.add(attempt.context().command().path());
            }
            showSuggestions(actor, input, suggestions);
            return;
        }

        if (visibleAttempts.size() == 1) {
            Potential<A> visibleAttempt = visibleAttempts.get(0);
            List<ExecutableCommand<A>> related = visibleAttempt.context().command().relatedCommands(actor).all();
            if (!related.isEmpty()) {
                List<String> suggestions = new ArrayList<>(related.size() + 1);
                suggestions.add(visibleAttempt.context().command().path());
                for (ExecutableCommand<A> command : related) {
                    suggestions.add(command.path());
                }
                showSuggestions(actor, input, suggestions);
                return;
            }
            visibleAttempt.handleException();
            return;
        }

        for (Potential<A> potential : failedAttempts) {
            if (!potential.context().command().permission().isExecutableBy(actor)) {
                handleNoPermission(potential.context());
                return;
            }
        }

        if (failedAttempts.size() == 1) {
            failedAttempts.get(0).handleException();
            return;
        }

        lamp(failedAttempts).handleException(new UnknownCommandException(input.peekUnquotedString()), ErrorContext.unknownCommand(actor));
    }

    private void handleNoPermission(@NotNull ExecutionContext<A> context) {
        try {
            context.command().permission().throwMissingPermission(context);
        } catch (Throwable throwable) {
            context.lamp().handleException(throwable, ErrorContext.executingFunction(context));
        }
    }

    private Lamp<A> lamp(@NotNull List<Potential<A>> failedAttempts) {
        return failedAttempts.get(0).context().lamp();
    }

    private void showSuggestions(@NotNull A actor, @NotNull StringStream input, @NotNull List<String> commands) {
        actor.error("Failed to find a suitable command for your input (\"" + input.source() + "\"). Did you mean:");
        for (int i = 0; i < commands.size(); i++) {
            if (i >= MAX_NUMBER_OF_SUGGESTIONS)
                break;
            actor.reply("- " + commands.get(i));
        }
    }
}
