package revxrsal.commands.cli.core;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.cli.ConsoleActor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.SenderResolver;

import java.io.PrintStream;
import java.util.Scanner;

enum CLISenderResolver implements SenderResolver {

    INSTANCE;

    @Override public boolean isCustomType(Class<?> type) {
        return PrintStream.class.isAssignableFrom(type) ||
                Scanner.class.isAssignableFrom(type);
    }

    @Override public @NotNull Object getSender(@NotNull Class<?> customSenderType, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
        ConsoleActor cActor = (ConsoleActor) actor;
        if (PrintStream.class.isAssignableFrom(customSenderType))
            return cActor.getOutputStream();
        return cActor.getScanner();
    }
}
