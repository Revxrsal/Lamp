package revxrsal.commands.bukkit.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.InvalidValueException;

/**
 * Thrown when an invalid value for a {@link org.bukkit.entity.Player} or a
 * {@link org.bukkit.OfflinePlayer} parameter is inputted in the command
 */
public class InvalidPlayerException extends InvalidValueException {

  public InvalidPlayerException(@NotNull CommandParameter parameter, @NotNull String input) {
    super(parameter, input);
  }
}
