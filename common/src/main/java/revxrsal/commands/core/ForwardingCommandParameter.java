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
package revxrsal.commands.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ParameterValidator;

/**
 * A utility class that forwards all calls to a delegate {@link CommandParameter}.
 */
abstract class ForwardingCommandParameter implements CommandParameter {

  protected ForwardingCommandParameter() { /* Constructor for use by subclasses */ }

  public abstract @NotNull CommandParameter delegate();

  @Override
  @NotNull
  public String getName() {
    return delegate().getName();
  }

  @Override
  @Nullable
  public String getDescription() {
    return delegate().getDescription();
  }

  @Override
  public int getMethodIndex() {
    return delegate().getMethodIndex();
  }

  @Override
  public int getCommandIndex() {
    return delegate().getCommandIndex();
  }

  @Override
  @NotNull
  public Class<?> getType() {
    return delegate().getType();
  }

  @Override
  public @NotNull Type getFullType() {
    return delegate().getFullType();
  }

  @Override
  public @NotNull @Unmodifiable List<String> getDefaultValue() {
    return delegate().getDefaultValue();
  }

  @Override
  public boolean consumesAllString() {
    return delegate().consumesAllString();
  }

  @Override
  public Parameter getJavaParameter() {
    return delegate().getJavaParameter();
  }

  @Override
  public @NotNull SuggestionProvider getSuggestionProvider() {
    return delegate().getSuggestionProvider();
  }

  @Override
  public @NotNull @Unmodifiable List<ParameterValidator<Object>> getValidators() {
    return delegate().getValidators();
  }

  @Override
  public boolean isOptional() {
    return delegate().isOptional();
  }

  @Override
  public boolean isLastInMethod() {
    return delegate().isLastInMethod();
  }

  @Override
  public boolean isSwitch() {
    return delegate().isSwitch();
  }

  @Override
  @NotNull
  public String getSwitchName() {
    return delegate().getSwitchName();
  }

  @Override
  public boolean isFlag() {
    return delegate().isFlag();
  }

  @Override
  @NotNull
  public String getFlagName() {
    return delegate().getFlagName();
  }

  @Override
  public boolean getDefaultSwitch() {
    return delegate().getDefaultSwitch();
  }

  @Override
  public @NotNull <T> ParameterResolver<T> getResolver() {
    return delegate().getResolver();
  }

  @Override
  public @NotNull CommandHandler getCommandHandler() {
    return delegate().getCommandHandler();
  }

  @Override
  public @NotNull ExecutableCommand getDeclaringCommand() {
    return delegate().getDeclaringCommand();
  }

  @Override
  public @NotNull CommandPermission getPermission() {
    return delegate().getPermission();
  }

  @Override
  public int compareTo(@NotNull CommandParameter o) {
    return delegate().compareTo(o);
  }

  @Override
  public <A extends Annotation> A getAnnotation(@NotNull Class<A> annotation) {
    return delegate().getAnnotation(annotation);
  }

  @Override
  public boolean hasAnnotation(@NotNull Class<? extends Annotation> annotation) {
    return delegate().hasAnnotation(annotation);
  }

  @Override
  public boolean hasPermission(@NotNull CommandActor actor) {
    return delegate().hasPermission(actor);
  }

  @Override
  public void checkPermission(@NotNull CommandActor actor) {
    delegate().checkPermission(actor);
  }

}
