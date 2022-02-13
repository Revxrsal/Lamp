package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.command.trait.CommandAnnotationHolder;
import revxrsal.commands.command.trait.PermissionHolder;

/**
 * Represents a convenient way to register custom {@link CommandPermission}
 * implementations. This reader can have access to a command's annotations.
 */
public interface PermissionReader {

    /**
     * Returns the specified permission for this command, or {@code null}
     * if this reader does not identify any permission.
     *
     * @param command Command to generate for. This will <em>always</em> be
     *                a {@link PermissionHolder}, and may be a {@link CommandParameter}
     *                or an {@link ExecutableCommand}.
     * @return The permission, or null if not identified.
     */
    @Nullable CommandPermission getPermission(@NotNull CommandAnnotationHolder command);

}
