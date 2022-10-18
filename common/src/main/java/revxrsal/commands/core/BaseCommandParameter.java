package revxrsal.commands.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.annotation.Flag;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ParameterValidator;

@RequiredArgsConstructor
final class BaseCommandParameter implements CommandParameter {

  private final String name;
  private final @Nullable String description;
  private final int index;
  private final @Unmodifiable List<String> def;
  private final boolean consumeString, optional;
  private final ExecutableCommand parent;
  SuggestionProvider suggestionProvider;
  private final Parameter parameter;
  @Nullable CommandPermission permission;
  ParameterResolver resolver;
  int cindex = -1;
  private final @Nullable Switch switchAnn;
  private final @Nullable Flag flag;
  private final List<ParameterValidator<Object>> validators;

  @Override
  public @NotNull String getName() {
    return name;
  }

  @Override
  public @Nullable String getDescription() {
    return description;
  }

  @Override
  public int getMethodIndex() {
    return index;
  }

  @Override
  public int getCommandIndex() {
    return cindex;
  }

  @Override
  public @NotNull Class<?> getType() {
    return parameter.getType();
  }

  @Override
  public @NotNull Type getFullType() {
    return parameter.getParameterizedType();
  }

  @Override
  public @NotNull @Unmodifiable List<String> getDefaultValue() {
    return def;
  }

  @Override
  public boolean consumesAllString() {
    return consumeString;
  }

  @Override
  public Parameter getJavaParameter() {
    return parameter;
  }

  @Override
  public @NotNull SuggestionProvider getSuggestionProvider() {
    return suggestionProvider;
  }

  @Override
  public @NotNull @Unmodifiable List<ParameterValidator<Object>> getValidators() {
    return validators;
  }

  @Override
  public boolean isOptional() {
    return optional || isSwitch();
  }

  @Override
  public boolean isLastInMethod() {
    return getMethodIndex() + 1 == parameter.getDeclaringExecutable().getParameterCount();
  }

  @Override
  public <A extends Annotation> A getAnnotation(@NotNull Class<A> annotation) {
    return parameter.getAnnotation(annotation);
  }

  @Override
  public boolean isSwitch() {
    return switchAnn != null;
  }

  @Override
  public @NotNull String getSwitchName() {
    if (!isSwitch()) {
      throw new IllegalStateException("Not a switch.");
    }
    return switchAnn.value().isEmpty() ? getName() : switchAnn.value();
  }

  @Override
  public boolean isFlag() {
    return flag != null;
  }

  @Override
  public @NotNull String getFlagName() {
    if (!isFlag()) {
      throw new IllegalStateException("Not a flag.");
    }
    return flag.value().isEmpty() ? getName() : flag.value();
  }

  @Override
  public boolean getDefaultSwitch() {
    if (!isSwitch()) {
      throw new IllegalStateException("Not a switch.");
    }
    return switchAnn.defaultValue();
  }

  @Override
  public boolean hasAnnotation(@NotNull Class<? extends Annotation> annotation) {
    return parameter.isAnnotationPresent(annotation);
  }

  @Override
  public @NotNull <T> ParameterResolver<T> getResolver() {
    return ((ParameterResolver<T>) resolver);
  }

  @Override
  public @NotNull CommandHandler getCommandHandler() {
    return parent.getCommandHandler();
  }

  @Override
  public @NotNull ExecutableCommand getDeclaringCommand() {
    return parent;
  }

  @Override
  public @NotNull CommandPermission getPermission() {
    return permission == null ? getDeclaringCommand().getPermission() : permission;
  }

  @Override
  public int compareTo(@NotNull CommandParameter o) {
    if (isFlag() && o.isSwitch()) {
      return 1;
    }
    if (isSwitch() && o.isFlag()) {
      return -1;
    }
    return Integer.compare(getCommandIndex(), o.getCommandIndex());
  }
}
