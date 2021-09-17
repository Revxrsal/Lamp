package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ResponseHandler;

import java.util.concurrent.CompletionStage;

/**
 * A response handler that appropriately handles return types of {@link CompletionStage}s.
 */
final class CompletionStageResponseHandler implements ResponseHandler<CompletionStage<Object>> {

    private final CommandHandler handler;
    private final ResponseHandler<Object> valueResponseHandler;

    public CompletionStageResponseHandler(CommandHandler handler, ResponseHandler<Object> valueResponseHandler) {
        this.handler = handler;
        this.valueResponseHandler = valueResponseHandler;
    }

    @Override
    public void handleResponse(CompletionStage<Object> response, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
        response.thenAccept(value -> {
            try {
                valueResponseHandler.handleResponse(value, actor, command);
            } catch (Throwable throwable) {
                handler.getExceptionHandler().handleException(throwable);
            }
        });
    }
}
