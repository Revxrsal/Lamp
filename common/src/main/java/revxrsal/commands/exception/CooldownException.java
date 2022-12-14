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
package revxrsal.commands.exception;

import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.util.Preconditions;

/**
 * Thrown when the {@link CommandActor} has to wait before executing a command again. This is set by
 * {@link Cooldown}.
 */
public class CooldownException extends RuntimeException {

  /**
   * The time left (in milliseconds)
   */
  private final long timeLeft;

  /**
   * Creates a new {@link CooldownException} with the given timestamp in milliseconds
   *
   * @param timeLeft The time left in milliseconds
   */
  public CooldownException(long timeLeft) {
    this.timeLeft = timeLeft;
  }

  /**
   * Creates a new {@link CooldownException} with the given timestamp in any unit
   *
   * @param unit     The time unit in which the time left is given
   * @param timeLeft The time left in the given unit
   */
  public CooldownException(TimeUnit unit, long timeLeft) {
    this.timeLeft = unit.toMillis(timeLeft);
  }

  /**
   * Returns the time left before being able to execute again
   *
   * @return Time left in milliseconds
   */
  public long getTimeLeftMillis() {
    return timeLeft;
  }

  /**
   * Returns the time left in the given unit
   *
   * @param unit Unit to convert to
   * @return The time left
   */
  public long getTimeLeft(@NotNull TimeUnit unit) {
    Preconditions.notNull(unit, "unit");
    return unit.convert(timeLeft, TimeUnit.MILLISECONDS);
  }
}
