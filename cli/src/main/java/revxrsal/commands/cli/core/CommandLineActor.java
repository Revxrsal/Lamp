package revxrsal.commands.cli.core;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.cli.ConsoleActor;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.UUID;

final class CommandLineActor implements ConsoleActor {

    private static final UUID CLI_UUID = new UUID(0, 0);
    private final CLIHandler handler;

    public CommandLineActor(CLIHandler handler) {
        this.handler = handler;
    }

    @Override public @NotNull InputStream getInputStream() {
        return handler.inputStream;
    }

    @Override public @NotNull PrintStream getOutputStream() {
        return handler.outputStream;
    }

    @Override public @NotNull PrintStream getErrorStream() {
        return handler.errorStream;
    }

    @Override public @NotNull Scanner getScanner() {
        return handler.scanner;
    }

    @Override @SneakyThrows public void close() {
        handler.close();
    }

    @Override public @NotNull String getName() {
        return "Command Line";
    }

    @Override public @NotNull UUID getUniqueId() {
        return CLI_UUID;
    }

    @Override public void reply(@NotNull String message) {
        getOutputStream().println(message);
    }

    @Override public void error(@NotNull String message) {
        getErrorStream().println(message);
    }
}
