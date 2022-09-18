package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.process.ContextResolver;
import dev.demeng.pluginbase.commands.process.ContextResolver.ContextResolverContext;
import dev.demeng.pluginbase.commands.process.ParameterResolver;
import dev.demeng.pluginbase.commands.process.ValueResolver;
import dev.demeng.pluginbase.commands.process.ValueResolver.ValueResolverContext;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

final class Resolver implements ParameterResolver<Object> {

  private final boolean mutates;

  private final ContextResolver<?> contextResolver;
  private final ValueResolver<?> valueResolver;

  public Resolver(ContextResolver<?> contextResolver, ValueResolver<?> valueResolver) {
    this.contextResolver = contextResolver;
    this.valueResolver = valueResolver;
    mutates = valueResolver != null;
  }

  @Override
  public boolean mutatesArguments() {
    return mutates;
  }

  @SneakyThrows
  public Object resolve(@NotNull ParameterResolverContext context) {
    if (valueResolver != null) {
      return valueResolver.resolve((ValueResolverContext) context);
    }
    return contextResolver.resolve((ContextResolverContext) context);
  }

  public static Resolver wrap(Object resolver) {
    if (resolver instanceof ValueResolver) {
      return new Resolver(null, (ValueResolver<?>) resolver);
    }
    return new Resolver((ContextResolver<?>) resolver, null);
  }

}
