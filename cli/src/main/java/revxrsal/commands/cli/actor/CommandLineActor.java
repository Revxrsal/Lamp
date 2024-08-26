package revxrsal.commands.cli.actor;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.cli.ConsoleActor;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.UUID;

record CommandLineActor(
        Lamp<ConsoleActor> lamp,
        InputStream inputStream,
        PrintStream outputStream,
        PrintStream errorStream,
        Scanner scanner
) implements ConsoleActor {

    private static final UUID CLI_UUID = new UUID(0, 0);

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
}
