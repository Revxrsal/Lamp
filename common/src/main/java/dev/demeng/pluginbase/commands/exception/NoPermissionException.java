package dev.demeng.pluginbase.commands.exception;

import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.CommandPermission;
import dev.demeng.pluginbase.commands.command.trait.PermissionHolder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown when a {@link CommandActor} lacks the required permission to execute the given command or
 * category.
 * <p>
 * Note that {@link #getCommand()} may be null when the user attempts to access a category in which
 * they do not have permission.
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class NoPermissionException extends RuntimeException {

  /**
   * The command being executed
   */
  private final @Nullable PermissionHolder command;

  /**
   * The permission the actor lacks
   */
  private final @NotNull CommandPermission permission;

}
