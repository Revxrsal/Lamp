/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
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
package revxrsal.commands.command.trait;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.exception.NoPermissionException;

/**
 * Represents a command component (category, parameter, etc.) that may hold its own permission.
 */
public interface PermissionHolder {

  /**
   * Returns the required permission to access this component.
   * <p>
   * Note that not all components may be able to explicitly declare permissions, and some
   * permissions will be inherited when appropriate.
   *
   * @return The command permission
   */
  @NotNull CommandPermission getPermission();

  /**
   * Returns whether the given command actor has permission to use this component.
   *
   * @param actor Actor to check against
   * @return {@code true} if they have the permission, false if otherwise.
   */
  default boolean hasPermission(@NotNull CommandActor actor) {
    return getPermission().canExecute(actor);
  }

  /**
   * Checks if the given command actor has this permission, otherwise throws a
   * {@link NoPermissionException}
   *
   * @param actor Actor to check against.
   */
  default void checkPermission(@NotNull CommandActor actor) {
    if (!getPermission().canExecute(actor)) {
      throw new NoPermissionException(this, getPermission());
    }
  }

}
