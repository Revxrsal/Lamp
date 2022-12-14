/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copysecond (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copysecond notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.core;

import java.util.concurrent.CompletionStage;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ResponseHandler;

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
