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
import revxrsal.commands.LampVisitor;
import revxrsal.commands.cli.actor.ActorFactory;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static revxrsal.commands.cli.CLIVisitors.consoleContextParameters;
import static revxrsal.commands.cli.CLIVisitors.consoleSenderResolver;

/**
 * Creates {@link Lamp} instances that contain relevant registrations
 * for CLI apps.
 */
public final class CLILamp {

    /**
     * Returns a {@link Lamp.Builder} that contains the default registrations
     * for CLI apps
     *
     * @return A {@link Lamp.Builder} containing the default configuration
     */
    public static @NotNull <A extends ConsoleActor> Lamp.Builder<A> builder() {
        return Lamp.<A>builder()
                .accept(consoleSenderResolver())
                .accept(consoleContextParameters());
    }

    /**
     * Polls the {@link System#in} input stream for input
     *
     * @return The visitor. Accept with {@link Lamp#accept(LampVisitor)}
     */
    public static @NotNull LampVisitor<ConsoleActor> pollStdin() {
        return poll(System.in, System.out, System.err, ActorFactory.defaultFactory());
    }

    /**
     * Polls the {@link System#in} input stream for input
     *
     * @param actorFactory The actor factory. This allows for supplying custom
     *                     implementations of {@link ConsoleActor}
     * @return The visitor. Accept with {@link Lamp#accept(LampVisitor)}
     */
    public static @NotNull <A extends ConsoleActor> LampVisitor<A> pollStdin(@NotNull ActorFactory<A> actorFactory) {
        return poll(System.in, System.out, System.err, actorFactory);
    }

    /**
     * Polls the {@link System#in} input stream for input
     *
     * @param inputStream  The input stream to poll
     * @param outputStream The output stream to write raw messages
     * @param errorStream  The output stream to write raw error messages
     * @param actorFactory The actor factory. This allows for supplying custom
     *                     implementations of {@link ConsoleActor}
     * @return The visitor. Accept with {@link Lamp#accept(LampVisitor)}
     */
    public static @NotNull <A extends ConsoleActor> LampVisitor<A> poll(
            @NotNull InputStream inputStream,
            @NotNull PrintStream outputStream,
            @NotNull PrintStream errorStream,
            @NotNull ActorFactory<A> actorFactory
    ) {
        Scanner scanner = new Scanner(inputStream);
        return lamp -> {
            A actor = actorFactory.create(inputStream, outputStream, errorStream, scanner, lamp);
            while (scanner.hasNext()) {
                String input = scanner.nextLine();
                lamp.dispatch(actor, input);
            }
        };
    }
}
