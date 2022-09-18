package dev.demeng.pluginbase.commands.help;

import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A writer for generating entries for help commands.
 *
 * @param <T> The command help entry type. If we want to merely use strings as what is generated for
 *            each command, this would be a {@link String}.
 *            <p>
 *            Similarly, more complex types, such as chat components, can have this type as the
 *            appropriate chat component type.
 */
public interface CommandHelpWriter<T> {

  /**
   * Generates a command help entry for the specified command
   *
   * @param command Command to generate for. It is generally advisable to do permission checks as
   *                well as other filters such as {@link ExecutableCommand#isSecret()}.
   * @param actor   Actor to generate for
   * @return The generated help entry. If null, this entry will not appear on the generated help
   * list.
   */
  @Nullable T generate(@NotNull ExecutableCommand command, @NotNull CommandActor actor);

  /**
   * Ignores any command that matches the specified predicate
   *
   * @param predicate Predicate to test for
   * @return The command help writer that filters according to that predicate.
   */
  default CommandHelpWriter<T> ignore(@NotNull Predicate<ExecutableCommand> predicate) {
    return (command, subject) -> {
      if (predicate.test(command)) {
        return null;
      }
      return generate(command, subject);
    };
  }

  /**
   * Writes commands that only matches the specified predicate
   *
   * @param predicate Predicate to test for
   * @return The command help writer that only allows elements that match the predicate.
   */
  default CommandHelpWriter<T> only(@NotNull Predicate<ExecutableCommand> predicate) {
    return ignore(predicate.negate());
  }

}
