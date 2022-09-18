package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.process.ContextResolverFactory;
import revxrsal.commands.process.ValueResolverFactory;

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
