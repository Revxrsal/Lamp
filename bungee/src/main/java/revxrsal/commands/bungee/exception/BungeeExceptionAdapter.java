package revxrsal.commands.bungee.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.DefaultExceptionHandler;

public class BungeeExceptionAdapter extends DefaultExceptionHandler {

    public static final BungeeExceptionAdapter INSTANCE = new BungeeExceptionAdapter();

    public void senderNotPlayer(@NotNull CommandActor actor, @NotNull SenderNotPlayerException exception) {
        actor.errorLocalized("must-be-player");
    }

    public void senderNotConsole(@NotNull CommandActor actor, @NotNull SenderNotConsoleException exception) {
        actor.errorLocalized("must-be-console");
    }

    public void invalidPlayer(@NotNull CommandActor actor, @NotNull InvalidPlayerException exception) {
        actor.errorLocalized("invalid-player", exception.getInput());
    }
}
