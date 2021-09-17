package revxrsal.commands.core;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ValueResolver;

final class Resolver implements ParameterResolver<Object> {

    private final boolean mutates;

    private final ContextResolver<?> contextResolver;
    private final ValueResolver<?> valueResolver;

    public Resolver(ContextResolver<?> contextResolver, ValueResolver<?> valueResolver) {
        this.contextResolver = contextResolver;
        this.valueResolver = valueResolver;
        mutates = valueResolver != null;
    }

    @Override public boolean mutatesArguments() {
        return mutates;
    }

    @SneakyThrows
    public Object resolve(@NotNull CommandActor actor,
                                        @NotNull CommandParameter parameter,
                                        @NotNull ExecutableCommand command,
                                        @NotNull ArgumentStack arguments) {
        if (valueResolver != null) {
            return valueResolver.resolve(arguments, actor, parameter, command);
        }
        return contextResolver.resolve(actor, parameter, command);
    }

    public static Resolver wrap(Object resolver) {
        if (resolver instanceof ValueResolver) {
            return new Resolver(null, (ValueResolver<?>) resolver);
        }
        return new Resolver((ContextResolver<?>) resolver, null);
    }

}
