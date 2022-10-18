/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
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
package revxrsal.commands.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.core.reflect.MethodCaller.BoundMethodCaller;
import revxrsal.commands.core.reflect.MethodCallerFactory;
import revxrsal.commands.util.ClassMap;

/**
 * An implementation of {@link CommandExceptionHandler} that inlines all exceptions into individual,
 * overridable methods. This greatly simplifies the process of handling exceptions.
 * <p>
 * This class loosely uses reflections to find the appropriate handler method. To handle custom
 * exceptions, extend this class and define a method that meets the following criteria:
 * <ol>
 *     <li>Method is public</li>
 *     <li>Method has 2 parameters, one is a CommandActor (or a subclass of it), and the
 *     other is your exception. The name of the method, and the order of parameters does
 *     not matter.</li>
 * </ol>
 * <p>
 * An example:
 * <pre>
 * {@code
 * public void onCustomException(CommandActor actor, CustomException e) {
 *     actor.error("Caught you!");
 * }
 * }
 * </pre>
 * If you have methods that meet the above criteria and want the reflection handler
 * to ignore them, annotate them with {@link Ignore}.
 */
public abstract class CommandExceptionAdapter implements CommandExceptionHandler {

  /**
   * An annotation to automatically ignore any method that may otherwise be a handler method.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Ignore {

  }

  @Ignore
  public void onUnhandledException(@NotNull CommandActor actor, @NotNull Throwable throwable) {
  }

  public void missingArgument(@NotNull CommandActor actor,
      @NotNull MissingArgumentException exception) {
  }

  public void invalidEnumValue(@NotNull CommandActor actor,
      @NotNull EnumNotFoundException exception) {
  }

  public void invalidUUID(@NotNull CommandActor actor, @NotNull InvalidUUIDException exception) {
  }

  public void invalidNumber(@NotNull CommandActor actor,
      @NotNull InvalidNumberException exception) {
  }

  public void invalidURL(@NotNull CommandActor actor, @NotNull InvalidURLException exception) {
  }

  public void invalidBoolean(@NotNull CommandActor actor,
      @NotNull InvalidBooleanException exception) {
  }

  public void numberNotInRange(@NotNull CommandActor actor,
      @NotNull NumberNotInRangeException exception) {
  }

  public void noPermission(@NotNull CommandActor actor, @NotNull NoPermissionException exception) {
  }

  public void argumentParse(@NotNull CommandActor actor,
      @NotNull ArgumentParseException exception) {
  }

  public void commandInvocation(@NotNull CommandActor actor,
      @NotNull CommandInvocationException exception) {
  }

  public void tooManyArguments(@NotNull CommandActor actor,
      @NotNull TooManyArgumentsException exception) {
  }

  public void invalidCommand(@NotNull CommandActor actor,
      @NotNull InvalidCommandException exception) {
  }

  public void invalidSubcommand(@NotNull CommandActor actor,
      @NotNull InvalidSubcommandException exception) {
  }

  public void noSubcommandSpecified(@NotNull CommandActor actor,
      @NotNull NoSubcommandSpecifiedException exception) {
  }

  public void cooldown(@NotNull CommandActor actor, @NotNull CooldownException exception) {
  }

  public void invalidHelpPage(@NotNull CommandActor actor,
      @NotNull InvalidHelpPageException exception) {
  }

  public void sendableException(@NotNull CommandActor actor, @NotNull SendableException exception) {
  }

  private static final List<Method> IGNORED_METHODS = new ArrayList<>();

  static {
    for (Method method : CommandExceptionAdapter.class.getDeclaredMethods()) {
      if (method.getParameterCount() != 2) {
        continue;
      }
      if (method.isAnnotationPresent(Ignore.class)) {
        IGNORED_METHODS.add(method);
      }
    }
  }

  @Override
  @Ignore
  public void handleException(@NotNull Throwable throwable, @NotNull CommandActor actor) {
    MethodExceptionHandler handler = handlers.getFlexibleOrDefault(throwable.getClass(),
        unknownHandler);
    if (handler == unknownHandler && throwable instanceof SelfHandledException) {
      ((SelfHandledException) throwable).handle(actor);
    } else {
      handler.handle(actor, throwable);
    }
  }

  public CommandExceptionAdapter() {
    for (Method m : getClass().getMethods()) {
      register(m);
    }
  }

  private final ClassMap<MethodExceptionHandler> handlers = new ClassMap<>();
  private final MethodExceptionHandler unknownHandler = this::onUnhandledException;

  @SneakyThrows
  private void register(@NotNull Method method) {
    if (!CommandExceptionAdapter.class.isAssignableFrom(method.getDeclaringClass())) {
      return;
    }
    if (method.getParameterCount() != 2) {
      return;
    }
    if (method.isAnnotationPresent(Ignore.class)) {
      return;
    }
    for (Method ignoredMethod : IGNORED_METHODS) {
      if (method.getName().equals(ignoredMethod.getName()) && Arrays.equals(
          method.getParameterTypes(), ignoredMethod.getParameterTypes())) {
        return;
      }
    }
    Parameter[] parameters = method.getParameters();
    Class<?> firstType = parameters[0].getType();
    Class<?> secondType = parameters[1].getType();
    Class<?> exceptionType;
    MethodExceptionHandler handler;
    if (CommandActor.class.isAssignableFrom(firstType) && Throwable.class.isAssignableFrom(
        secondType)) {
      exceptionType = secondType;
      BoundMethodCaller caller = MethodCallerFactory.defaultFactory().createFor(method)
          .bindTo(this);
      handler = caller::call;
    } else if (Throwable.class.isAssignableFrom(firstType) && CommandActor.class.isAssignableFrom(
        secondType)) {
      exceptionType = firstType;
      BoundMethodCaller caller = MethodCallerFactory.defaultFactory().createFor(method)
          .bindTo(this);
      handler = (actor, throwable) -> caller.call(throwable, actor);
    } else {
      return;
    }
    handlers.add(exceptionType, handler);
  }

  private interface MethodExceptionHandler {

    void handle(@NotNull CommandActor actor, @NotNull Throwable throwable);


  }

}
