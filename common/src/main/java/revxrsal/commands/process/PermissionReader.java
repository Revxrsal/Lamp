package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;

/**
 * Represents a convenient way to register custom {@link CommandPermission}
 * implementations. This reader can have access to a command's annotations.
 */
public interface PermissionReader {

    /**
     * Returns the specified permission for this command, or {@code null}
     * if this reader does not identify any permission.
     *
     * @param command Command to generate for
     * @return The permission, or null if not identified.
     */
    @Nullable CommandPermission getPermission(@NotNull ExecutableCommand command);

}
