package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ResponseHandler;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * A response handler that appropriately handles return types of {@link Optional}s.
 */
final class SupplierResponseHandler implements ResponseHandler<Supplier<Object>> {

    private final ResponseHandler<Object> delegate;

    public SupplierResponseHandler(ResponseHandler<Object> delegate) {
        this.delegate = delegate;
    }

    @Override public void handleResponse(Supplier<Object> response, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
        delegate.handleResponse(response.get(), actor, command);
    }
}
