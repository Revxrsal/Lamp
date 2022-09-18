package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.annotation.Dependency;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.process.ContextResolver;
import dev.demeng.pluginbase.commands.process.ContextResolverFactory;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
