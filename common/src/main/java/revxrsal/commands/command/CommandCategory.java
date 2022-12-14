/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copysecond (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copysecond notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.command;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.command.trait.PermissionHolder;
import revxrsal.commands.core.CommandPath;

/**
 * Represents a command category.
 * <p>
 * Command categories do not have explicit actions by themselves, however they can include the
 * following:
 * <ul>
 *     <li>Subcommands, which are executable commands that perform specific actions</li>
 *     <li>Subactions, such as {@link Default}.</li>
 * </ul>
 */
public interface CommandCategory extends PermissionHolder, Comparable<CommandCategory> {

  /**
   * Returns the name of this category
   *
   * @return The category name
   */
  @NotNull String getName();

  /**
   * Returns the full command path to this category
   *
   * @return The command path
   */
  @NotNull CommandPath getPath();

  /**
   * Returns the command handler that instantiated this category
   *
   * @return The owning command handler
   */
  @NotNull CommandHandler getCommandHandler();

  /**
   * Returns the parent category of this category. This can be null in case of root categories.
   *
   * @return The parent category
   */
  @Nullable CommandCategory getParent();

  /**
   * Returns the {@link ExecutableCommand} of this category that is executed when no arguments are
   * supplied for the category.
   *
   * @return The category's default action
   * @see Default
   */
  @Nullable ExecutableCommand getDefaultAction();

  /**
   * Returns the required permission to access this category.
   * <p>
   * Command categories by default do not have explicit permissions, therefore having access to the
   * category is having access to any of its children commands or categories.
   *
   * @return The command permission
   */
  @NotNull CommandPermission getPermission();

  /**
   * Returns whether is this category secret or not. This will only return true if all the children
   * categories and commands of this category are secret.
   *
   * @return Is this category secret or not.
   */
  boolean isSecret();

  /**
   * Returns whether is this command category empty or not.
   * <p>
   * A command category is empty if it has no sub-commands, no sub-categories, and no default
   * action.
   *
   * @return Is this category empty or not
   */
  boolean isEmpty();

  /**
   * Returns an unmodifiable view of all the sub-categories in this category.
   *
   * @return The sub-categories
   */
  @NotNull @UnmodifiableView Map<CommandPath, CommandCategory> getCategories();

  /**
   * Returns an unmodifiable view of all the commands in this category.
   *
   * @return The subcommands
   */
  @NotNull @UnmodifiableView Map<CommandPath, ExecutableCommand> getCommands();

}
