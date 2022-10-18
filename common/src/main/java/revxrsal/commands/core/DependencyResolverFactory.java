package revxrsal.commands.core;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ContextResolverFactory;

enum DependencyResolverFactory implements ContextResolverFactory {

  INSTANCE;

  @Override
  public @Nullable ContextResolver<?> create(@NotNull CommandParameter parameter) {
    if (!parameter.hasAnnotation(Dependency.class)) {
      return null;
    }
    Supplier<?> value = parameter.getCommandHandler().getDependency(parameter.getType());
    if (value == null) {
      throw new IllegalArgumentException("Unable to resolve dependency for parameter " +
          parameter.getName() + " in " + parameter.getDeclaringCommand().getPath().toRealString());
    }
    return context -> value.get();
  }
}
