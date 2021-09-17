package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ContextResolverFactory;
import revxrsal.commands.annotation.Dependency;

import java.util.function.Supplier;

enum DependencyResolverFactory implements ContextResolverFactory {

    INSTANCE;

    @Override public @Nullable ContextResolver<?> create(@NotNull CommandParameter parameter) {
        if (!parameter.hasAnnotation(Dependency.class)) return null;
        Supplier<?> value = parameter.getCommandHandler().getDependency(parameter.getType());
        if (value == null)
            throw new IllegalArgumentException("Unable to resolve dependency for parameter " +
                    parameter.getName() + " in " + parameter.getDeclaringCommand().getPath().toRealString());
        return (actor, parameter1, command) -> value.get();
    }
}
