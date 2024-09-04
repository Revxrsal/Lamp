package revxrsal.commands.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.Potential;

import java.util.List;

/**
 * The default failure handler. This can be overridden in {@link DispatcherSettings}
 * from {@link Lamp.Builder#dispatcherSettings()}.
 * <p>
 * This will simply handle the exception of each failed attempt.
 *
 * @param <A> The actor type
 */
final class DefaultFailureHandler<A extends CommandActor> implements FailureHandler<A> {

    private static final DefaultFailureHandler<CommandActor> INSTANCE = new DefaultFailureHandler<>();

    @Override
    public void handleFailedAttempts(@NotNull @Unmodifiable List<Potential<A>> failedAttempts) {
        for (Potential<A> failedAttempt : failedAttempts) {
            failedAttempt.handleException();
        }
    }

    @SuppressWarnings("unchecked")
    public static <A extends CommandActor> FailureHandler<A> defaultFailureHandler() {
        return (FailureHandler<A>) INSTANCE;
    }

}