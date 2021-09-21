package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ResponseHandler;

import java.util.Optional;

/**
 * A response handler that appropriately handles return types of {@link Optional}s.
 */
final class OptionalResponseHandler implements ResponseHandler<Optional<Object>> {

    private final ResponseHandler<Object> delegate;

    public OptionalResponseHandler(ResponseHandler<Object> delegate) {
        this.delegate = delegate;
    }

    @Override public void handleResponse(Optional<Object> response, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
        response.ifPresent(v -> delegate.handleResponse(v, actor, command));
    }
}
