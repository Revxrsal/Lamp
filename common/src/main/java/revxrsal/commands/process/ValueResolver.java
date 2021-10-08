package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Single;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.process.ParameterResolver.ParameterResolverContext;

/**
 * A resolver for resolving values that, by default, require data from the arguments
 * to resolve their value.
 * An example context resolver is finding a player from their name.
 *
 * @param <T> The resolved type
 */
public interface ValueResolver<T> {

    /**
     * Resolves the value of this resolver
     *
     * @param context The command resolving context.
     * @return The resolved value. May or may not be null.
     * @throws Throwable Any exceptions that should be handled by {@link CommandExceptionHandler}
     */
    T resolve(@NotNull ValueResolverContext context) throws Throwable;

    /**
     * Represents the resolving context of {@link ValueResolver}. This contains
     * all the relevant information about the resolving context.
     */
    interface ValueResolverContext extends ParameterResolverContext {

        /**
         * Returns the command arguments passed to the command.
         * <p>
         * This stack may have been modified by previous resolvers, and
         * there is no guarantee that its content matches the actor input.
         * <p>
         * To access the original, unmodified input of the actor, see {@link #input()}.
         *
         * @return The argument stack
         */
        ArgumentStack arguments();

        /**
         * Returns (and removes) the string in which might get concatenated with the rest
         * of the arguments if the {@link #parameter()} consumes all strings that follow it.
         * <p>
         * This is equivilent to calling {@code arguments().popForParameter(parameter())}
         *
         * @return The string for this parameter. Will return the first argument if the parameter cannot
         * consume all strings.
         * @see CommandParameter#consumesAllString()
         * @see Single
         * @see ArgumentStack#popForParameter(CommandParameter).
         */
        String popForParameter();

        /**
         * Returns (and removes) the first value in the {@link #arguments() argument stack}.
         *
         * @return The first value.
         * @throws java.util.NoSuchElementException if the stack is empty
         */
        String pop();

    }

}
