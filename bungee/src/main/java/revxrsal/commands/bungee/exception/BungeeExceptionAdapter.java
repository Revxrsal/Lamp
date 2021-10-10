package revxrsal.commands.bungee.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.DefaultExceptionHandler;

public class BungeeExceptionAdapter extends DefaultExceptionHandler {

    public static final BungeeExceptionAdapter INSTANCE = new BungeeExceptionAdapter();

    @Override protected final void handleUnknown(@NotNull CommandActor actor, @NotNull Throwable throwable) {
        if (throwable instanceof SenderNotPlayerException)
            senderNotPlayer(actor, (SenderNotPlayerException) throwable);
        else if (throwable instanceof SenderNotConsoleException)
            senderNotConsole(actor, (SenderNotConsoleException) throwable);
        else if (throwable instanceof InvalidPlayerException)
            invalidPlayer(actor, (InvalidPlayerException) throwable);
        else
            handleUnknownThrowable(actor, throwable);
    }

    protected void senderNotPlayer(@NotNull CommandActor actor, @NotNull SenderNotPlayerException exception) {
        actor.error("You must be a player to use this command!");
    }

    protected void senderNotConsole(@NotNull CommandActor actor, @NotNull SenderNotConsoleException exception) {
        actor.error("This command can only be used on console!");
    }

    protected void invalidPlayer(@NotNull CommandActor actor, @NotNull InvalidPlayerException exception) {
        actor.error("Invalid player: &e" + exception.getInput());
    }

    protected void handleUnknownThrowable(@NotNull CommandActor actor, @NotNull Throwable throwable) {
        throwable.printStackTrace();
    }
}
