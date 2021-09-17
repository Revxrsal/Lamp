package revxrsal.commands.cli;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.cli.core.CLIHandler;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * A {@link revxrsal.commands.CommandHandler} adapted for use in command-line applications
 */
public interface ConsoleCommandHandler extends CommandHandler, AutoCloseable {

    /**
     * Returns the singleton {@link ConsoleActor} used by this console
     * handler
     *
     * @return The console actor
     */
    @NotNull ConsoleActor getConsole();

    /**
     * Polls the command-line for input. This should only be called
     * after everything has been registered!
     */
    void pollInput();

    /**
     * Creates a {@link ConsoleCommandHandler} that takes input and
     * sends output to the {@link System}'s default streams.
     *
     * @return The newly created console command handler
     */
    static @NotNull ConsoleCommandHandler create() {
        return new CLIHandler(System.in, System.out, System.err);
    }

    /**
     * Creates a {@link ConsoleCommandHandler} that takes input from
     * the given input stream and sends output to the {@link System}'s
     * default output streams.
     *
     * @param inputStream Input stream to poll input from
     * @return The newly created console command handler
     */
    static @NotNull ConsoleCommandHandler create(@NotNull InputStream inputStream) {
        return new CLIHandler(inputStream, System.out, System.err);
    }

    /**
     * Creates a {@link ConsoleCommandHandler} that takes input from
     * the given input stream and sends all output to the given output stream
     *
     * @param inputStream  Input stream to poll input from
     * @param outputStream Output stream to send normal messages to. This will also
     *                     be used for errors.
     * @return The newly created console command handler
     */
    static @NotNull ConsoleCommandHandler create(@NotNull InputStream inputStream,
                                                 @NotNull PrintStream outputStream) {
        return new CLIHandler(inputStream, outputStream);
    }

    /**
     * Creates a {@link ConsoleCommandHandler} that takes input from
     * the given input stream and sends each input to the specified stream
     *
     * @param inputStream  Input stream to poll input from
     * @param outputStream Output stream to send normal messages to.
     * @param errorStream  Output stream to send error messages to.
     * @return The newly created console command handler
     */
    static @NotNull ConsoleCommandHandler create(@NotNull InputStream inputStream,
                                                 @NotNull PrintStream outputStream,
                                                 @NotNull PrintStream errorStream) {
        return new CLIHandler(inputStream, outputStream, errorStream);
    }
}
