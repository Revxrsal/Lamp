package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.process.ContextResolverFactory;
import dev.demeng.pluginbase.commands.process.ValueResolverFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class ResolverFactory {

  private final Object factory;

  public ResolverFactory(Object factory) {
    this.factory = factory;
  }

  public @Nullable Resolver create(@NotNull CommandParameter parameter) {
    Object resolver;

    if (factory instanceof ContextResolverFactory) {
      resolver = ((ContextResolverFactory) factory).create(parameter);
    } else {
      resolver = ((ValueResolverFactory) factory).create(parameter);
    }

    if (resolver == null) {
      return null;
    }
    return Resolver.wrap(resolver);
  }

}
