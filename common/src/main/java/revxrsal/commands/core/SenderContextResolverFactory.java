package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.NotSender;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ContextResolverFactory;
import revxrsal.commands.process.SenderResolver;

import java.util.List;

import static revxrsal.commands.util.Preconditions.notNull;

final class SenderContextResolverFactory implements ContextResolverFactory {

    private static final SenderResolver SELF = new SenderResolver() {

        @Override public boolean isCustomType(Class<?> type) {
            return CommandActor.class.isAssignableFrom(type);
        }

        @Override public @NotNull Object getSender(@NotNull Class<?> customSenderType,
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

    @Override public @Nullable ContextResolver<?> create(@NotNull CommandParameter parameter) {
        if (parameter.getMethodIndex() != 0) return null;
        if (parameter.isOptional() || parameter.hasAnnotation(NotSender.class)) return null;
        for (SenderResolver resolver : resolvers) {
            if (resolver.isCustomType(parameter.getType())) {
                return context -> notNull(resolver.getSender(parameter.getType(), context.actor(), context.command()),
                        "SenderResolver#getSender() must not return null!");
            }
        }
        return null;
    }
}
