package revxrsal.commands.cli;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Represents the {@link CommandActor} of a command-line application
 */
public interface ConsoleActor extends CommandActor {

    /**
     * Returns the {@link InputStream} that is being used to take input
     * from the console.
     *
     * @return The console's input stream.
     */
    @NotNull InputStream inputStream();

    /**
     * Returns the {@link PrintStream} that is being used to send normal
     * output to the console.
     *
     * @return The console's output stream.
     */
    @NotNull PrintStream outputStream();

    /**
     * Returns the {@link PrintStream} that is being used to send errors
     * to the console.
     *
     * @return The console's output stream.
     */
    @NotNull PrintStream errorStream();

    /**
     * Returns the {@link Scanner} that is being polled for input.
     *
     * @return The console scanner.
     */
    @NotNull Scanner scanner();

}
