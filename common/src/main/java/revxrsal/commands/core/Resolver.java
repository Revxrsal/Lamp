package revxrsal.commands.core;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ContextResolver.ContextResolverContext;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ValueResolver;
import revxrsal.commands.process.ValueResolver.ValueResolverContext;

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
