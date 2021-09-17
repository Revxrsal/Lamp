package revxrsal.commands.cli.core;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.cli.ConsoleActor;
import revxrsal.commands.cli.ConsoleCommandHandler;
import revxrsal.commands.core.BaseCommandHandler;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static revxrsal.commands.util.Preconditions.notNull;

public final class CLIHandler extends BaseCommandHandler implements ConsoleCommandHandler {

    final InputStream inputStream;
    final PrintStream outputStream, errorStream;
    final Scanner scanner;
    private final ConsoleActor actor;

    public CLIHandler(InputStream inputStream, PrintStream outputStream, PrintStream errorStream) {
        super();
        this.inputStream = notNull(inputStream, "input stream");
        this.outputStream = notNull(outputStream, "output stream");
        this.errorStream = notNull(errorStream, "error stream");
        scanner = new Scanner(inputStream);
        actor = new CommandLineActor(this);
        registerSenderResolver(CLISenderResolver.INSTANCE);
    }

    public CLIHandler(InputStream inputStream, PrintStream outputStream) {
        this(inputStream, outputStream, outputStream);
    }

    @Override public @NotNull ConsoleActor getConsole() {
        return actor;
    }

    @Override public void pollInput() {
        while (scanner.hasNext()) {
            String input = scanner.nextLine();
            dispatch(actor, input);
        }
    }

    @Override public void close() {
        scanner.close();
        outputStream.close();
    }
}
