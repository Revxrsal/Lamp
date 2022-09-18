package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.annotation.NotSender;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.CommandParameter;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.process.ContextResolver;
import dev.demeng.pluginbase.commands.process.ContextResolverFactory;
import dev.demeng.pluginbase.commands.process.SenderResolver;
import dev.demeng.pluginbase.commands.util.Preconditions;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SenderContextResolverFactory implements ContextResolverFactory {

  private static final SenderResolver SELF = new SenderResolver() {

    @Override
    public boolean isCustomType(Class<?> type) {
      return CommandActor.class.isAssignableFrom(type);
    }

    @Override
    public @NotNull Object getSender(@NotNull Class<?> customSenderType,
        @NotNull CommandActor actor,
        @NotNull ExecutableCommand command) {
      return actor;
    }
  };

  private final List<SenderResolver> resolvers;

  public SenderContextResolverFactory(List<SenderResolver> resolvers) {
    this.resolvers = resolvers;
    resolvers.add(SELF);
  }

  @Override
  public @Nullable ContextResolver<?> create(@NotNull CommandParameter parameter) {
    if (parameter.getMethodIndex() != 0) {
      return null;
    }
    if (parameter.isOptional() || parameter.hasAnnotation(NotSender.class)) {
      return null;
    }
    for (SenderResolver resolver : resolvers) {
      if (resolver.isCustomType(parameter.getType())) {
        return context -> Preconditions.notNull(
            resolver.getSender(parameter.getType(), context.actor(), context.command()),
            "SenderResolver#getSender() must not return null!");
      }
    }
    return null;
  }
}
