package revxrsal.commands.core;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ResponseHandler;

/**
 * A response handler that appropriately handles return types of {@link Supplier}s.
 */
final class SupplierResponseHandler implements ResponseHandler<Supplier<Object>> {

  private final ResponseHandler<Object> delegate;

  public SupplierResponseHandler(ResponseHandler<Object> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void handleResponse(Supplier<Object> response, @NotNull CommandActor actor,
      @NotNull ExecutableCommand command) {
    try {
      delegate.handleResponse(response.get(), actor, command);
    } catch (Throwable throwable) {
      command.getCommandHandler().getExceptionHandler().handleException(throwable, actor);
    }
  }
}
