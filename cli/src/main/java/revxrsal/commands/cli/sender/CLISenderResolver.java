package revxrsal.commands.cli.sender;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.cli.ConsoleActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.SenderResolver;

import java.io.PrintStream;
import java.util.Scanner;

public enum CLISenderResolver implements SenderResolver<ConsoleActor> {

    INSTANCE;

    @Override public boolean isSenderType(@NotNull CommandParameter parameter) {
        Class<?> type = parameter.type();
        return PrintStream.class.isAssignableFrom(type) ||
                Scanner.class.isAssignableFrom(type);
    }

    @Override
    public @NotNull Object getSender(@NotNull Class<?> customSenderType, @NotNull ConsoleActor actor, @NotNull ExecutableCommand<ConsoleActor> command) {
        if (PrintStream.class.isAssignableFrom(customSenderType))
            return actor.outputStream();
        return actor.scanner();
    }
}
