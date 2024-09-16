package revxrsal.commands.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.Potential;
import revxrsal.commands.stream.StringStream;

import java.util.List;

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
        if (failedAttempts.size() == 1) {
            failedAttempts.get(0).handleException();
            return;
        }
        actor.error("Failed to find a suitable command for your input (\"" + input.source() + "\"). Did you mean:");
        for (int i = 0; i < failedAttempts.size(); i++) {
            if (i >= MAX_NUMBER_OF_SUGGESTIONS)
                break;
            Potential<A> failedAttempt = failedAttempts.get(i);
            actor.reply("- " + failedAttempt.context().command().path());
        }
    }
}