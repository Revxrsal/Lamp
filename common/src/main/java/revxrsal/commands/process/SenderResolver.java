package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;

/**
 * Represents a special resolver for parameters that are always first in
 * command methods. These parameters can be treated as referring to the
 * command actor.
 * <p>
 * Registering a custom sender resolver allows using certain types as
 * senders. The custom types must be tested inside {@link #isCustomType(Class)},
 * and it is recommended to use {@code CustomType.class.isAssignableFrom(type)} to
 * make sure subclasses are respected.
 * <p>
 * Register with {@link CommandHandler#registerSenderResolver(SenderResolver)}.
 */
public interface SenderResolver {

    /**
     * Tests whether is the specified type a custom sender type or not
     *
     * @param type Type to test
     * @return True if it is a custom type, false if otherwise.
     */
    boolean isCustomType(Class<?> type);

    /**
     * Returns the custom sender value from the given context
     *
     * @param customSenderType The type of the custom sender. This matches
     *                         the command parameter type.
     * @param actor            The command actor
     * @param command          The command being executed
     * @return The custom sender value. This must not be null.
     */
    @NotNull Object getSender(@NotNull Class<?> customSenderType,
                              @NotNull CommandActor actor,
                              @NotNull ExecutableCommand command);

}
