package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.process.ResponseHandler;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * A response handler that appropriately handles return types of {@link Optional}s.
 */
final class OptionalResponseHandler implements ResponseHandler<Optional<Object>> {

  private final ResponseHandler<Object> delegate;

  public OptionalResponseHandler(ResponseHandler<Object> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void handleResponse(Optional<Object> response, @NotNull CommandActor actor,
      @NotNull ExecutableCommand command) {
    response.ifPresent(v -> delegate.handleResponse(v, actor, command));
  }
}
