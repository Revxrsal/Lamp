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
package revxrsal.commands.help;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;

import java.util.function.Predicate;

/**
 * A writer for generating entries for help commands.
 *
 * @param <T> The command help entry type. If we want to merely
 *            use strings as what is generated for each command,
 *            this would be a {@link String}.
 *            <p>
 *            Similarly, more complex types, such as chat components,
 *            can have this type as the appropriate chat component type.
 */
public interface CommandHelpWriter<T> {

    /**
     * Generates a command help entry for the specified command
     *
     * @param command Command to generate for. It is generally advisable to
     *                do permission checks as well as other filters such
     *                as {@link ExecutableCommand#isSecret()}.
     * @param actor   Actor to generate for
     * @return The generated help entry. If null, this entry will not
     * appear on the generated help list.
     */
    @Nullable T generate(@NotNull ExecutableCommand command, @NotNull CommandActor actor);

    /**
     * Ignores any command that matches the specified predicate
     *
     * @param predicate Predicate to test for
     * @return The command help writer that filters according to
     * that predicate.
     */
    default CommandHelpWriter<T> ignore(@NotNull Predicate<ExecutableCommand> predicate) {
        return (command, subject) -> {
            if (predicate.test(command)) return null;
            return generate(command, subject);
        };
    }

    /**
     * Writes commands that only matches the specified predicate
     *
     * @param predicate Predicate to test for
     * @return The command help writer that only allows elements that match
     * the predicate.
     */
    default CommandHelpWriter<T> only(@NotNull Predicate<ExecutableCommand> predicate) {
        return ignore(predicate.negate());
    }

}
