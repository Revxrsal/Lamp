package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.CommandExceptionHandler;

import java.util.function.Supplier;

/**
 * A resolver for resolving values that are, by default, resolvable through the command
 * invocation context, and do not need any data from the arguments to find the value.
 * An example context resolver is finding the sender's world.
 *
 * @param <T> The resolved type
 */
public interface ContextResolver<T> {

    /**
     * Resolves the value of this resolver
     *
     * @param actor     The command actor
     * @param parameter The parameter to resolve
     * @return The resolved value. May or may not be null.
     * @throws Throwable Any exceptions that should be handled by {@link CommandExceptionHandler}
     */
    T resolve(@NotNull CommandActor actor,
              @NotNull CommandParameter parameter,
              @NotNull ExecutableCommand command) throws Throwable;

    /**
     * Returns a context resolver that returns a static value. This
     * is a simpler way for adding constant values without having to
     * deal with lambdas.
     *
     * @param value The value to return
     * @param <T>   The value type
     * @return The context resolver
     * @since 1.3.0
     */
    static <T> ContextResolver<T> of(@NotNull T value) {
        return (actor, parameter, command) -> value;
    }

    /**
     * Returns a context resolver that returns a supplier value. This
     * is a simpler way for adding values without having to deal
     * with lambdas.
     *
     * @param value The value supplier
     * @param <T>   The value type
     * @return The context resolver
     * @since 1.3.0
     */
    static <T> ContextResolver<T> of(@NotNull Supplier<T> value) {
        return (actor, parameter, command) -> value.get();
    }
}
