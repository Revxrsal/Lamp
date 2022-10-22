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
