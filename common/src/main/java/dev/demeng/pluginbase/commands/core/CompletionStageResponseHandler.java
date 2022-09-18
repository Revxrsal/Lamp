package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.process.ResponseHandler;
import java.util.concurrent.CompletionStage;
import org.jetbrains.annotations.NotNull;

/**
 * A response handler that appropriately handles return types of {@link CompletionStage}s.
 */
final class CompletionStageResponseHandler implements ResponseHandler<CompletionStage<Object>> {

  private final CommandHandler handler;
  private final ResponseHandler<Object> delegate;

  public CompletionStageResponseHandler(CommandHandler handler, ResponseHandler<Object> delegate) {
    this.handler = handler;
    this.delegate = delegate;
  }

  @Override
  public void handleResponse(CompletionStage<Object> response, @NotNull CommandActor actor,
      @NotNull ExecutableCommand command) {
    response.whenComplete((value, exception) -> {
      if (exception != null) {
        handler.getExceptionHandler().handleException(exception, actor);
      } else {
        try {
          delegate.handleResponse(value, actor, command);
        } catch (Throwable throwable) {
          handler.getExceptionHandler().handleException(throwable, actor);
        }
      }
    });
  }
}
