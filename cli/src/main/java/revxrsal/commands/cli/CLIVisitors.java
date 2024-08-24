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
package revxrsal.commands.cli;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.cli.sender.CLISenderResolver;
import revxrsal.commands.process.SenderResolver;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Includes modular building blocks for hooking into the CLI applications.
 * <p>
 * Accept individual functions using {@link Lamp.Builder#accept(LampBuilderVisitor)}
 */
public final class CLIVisitors {

    /**
     * Registers context parameters for the following types:
     * <ul>
     *     <li>{@link Scanner}</li>
     *     <li>{@link PrintStream}</li>
     * </ul>
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static @NotNull <A extends ConsoleActor> LampBuilderVisitor<A> consoleContextParameters() {
        return builder -> builder.parameterTypes()
                .addContextParameterLast(Scanner.class, (parameter, input, context) -> context.actor().scanner())
                .addContextParameterLast(PrintStream.class, (parameter, input, context) -> context.actor().outputStream());
    }

    /**
     * Registers {@link SenderResolver a sender resolver} for the following types:
     * <ul>
     *     <li>{@link Scanner}</li>
     *     <li>{@link PrintStream}</li>
     * </ul>
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static @NotNull <A extends ConsoleActor> LampBuilderVisitor<A> consoleSenderResolver() {
        return builder -> builder.senderResolver(CLISenderResolver.INSTANCE);
    }
}
