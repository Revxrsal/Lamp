package revxrsal.commands.cli;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;

import java.io.Closeable;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Represents the {@link CommandActor} of a command-line application
 */
public interface ConsoleActor extends CommandActor, Closeable {

    /**
     * Returns the {@link InputStream} that is being used to take input
     * from the console.
     *
     * @return The console's input stream.
     */
    @NotNull InputStream getInputStream();

    /**
     * Returns the {@link PrintStream} that is being used to send normal
     * output to the console.
     *
     * @return The console's output stream.
     */
    @NotNull PrintStream getOutputStream();

    /**
     * Returns the {@link PrintStream} that is being used to send errors
     * to the console.
     *
     * @return The console's output stream.
     */
    @NotNull PrintStream getErrorStream();

    /**
     * Returns the {@link Scanner} that is being polled for input.
     *
     * @return The console scanner.
     */
    @NotNull Scanner getScanner();

}
