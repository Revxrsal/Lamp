package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.process.ResponseHandler;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

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
