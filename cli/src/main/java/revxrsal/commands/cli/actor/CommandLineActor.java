package revxrsal.commands.cli.actor;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.cli.ConsoleActor;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

final class CommandLineActor implements ConsoleActor {

    private static final UUID CLI_UUID = new UUID(0, 0);
    private final Lamp<ConsoleActor> lamp;
    private final InputStream inputStream;
    private final PrintStream outputStream;
    private final PrintStream errorStream;
    private final Scanner scanner;

    CommandLineActor(
            Lamp<ConsoleActor> lamp,
            InputStream inputStream,
            PrintStream outputStream,
            PrintStream errorStream,
            Scanner scanner
    ) {
        this.lamp = lamp;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.errorStream = errorStream;
        this.scanner = scanner;
    }

    @Override public @NotNull String name() {
        return "Command Line";
    }

    @Override public @NotNull UUID uniqueId() {
        return CLI_UUID;
    }

    @Override public void sendRawMessage(@NotNull String message) {
        outputStream.println(message);
    }

    @Override public void sendRawError(@NotNull String message) {
        errorStream.println(message);
    }

    @Override public Lamp<ConsoleActor> lamp() {return lamp;}

    @Override public @NotNull InputStream inputStream() {return inputStream;}

    @Override public @NotNull PrintStream outputStream() {return outputStream;}

    @Override public @NotNull PrintStream errorStream() {return errorStream;}

    @Override public @NotNull Scanner scanner() {return scanner;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        CommandLineActor that = (CommandLineActor) obj;
        return Objects.equals(this.lamp, that.lamp) &&
                Objects.equals(this.inputStream, that.inputStream) &&
                Objects.equals(this.outputStream, that.outputStream) &&
                Objects.equals(this.errorStream, that.errorStream) &&
                Objects.equals(this.scanner, that.scanner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lamp, inputStream, outputStream, errorStream, scanner);
    }

    @Override
    public String toString() {
        return "CommandLineActor[" +
                "lamp=" + lamp + ", " +
                "inputStream=" + inputStream + ", " +
                "outputStream=" + outputStream + ", " +
                "errorStream=" + errorStream + ", " +
                "scanner=" + scanner + ']';
    }

}
