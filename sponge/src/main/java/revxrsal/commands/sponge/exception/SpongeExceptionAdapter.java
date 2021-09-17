package revxrsal.commands.sponge.exception;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.util.TextMessageException;
import revxrsal.commands.exception.DefaultExceptionHandler;

public /*final*/ class SpongeExceptionAdapter extends DefaultExceptionHandler {

    public static final SpongeExceptionAdapter INSTANCE = new SpongeExceptionAdapter();

    @Override protected final void handleUnknown(@NotNull Throwable throwable) {
        if (throwable instanceof SenderNotPlayerException)
            senderNotPlayer((SenderNotPlayerException) throwable);
        else if (throwable instanceof SenderNotConsoleException)
            senderNotConsole((SenderNotConsoleException) throwable);
        else if (throwable instanceof InvalidPlayerException)
            invalidPlayer((InvalidPlayerException) throwable);
        else if (throwable instanceof InvalidWorldException)
            invalidWorld((InvalidWorldException) throwable);
        else
            handleUnknownThrowable(throwable);
    }

    protected void senderNotPlayer(@NotNull SenderNotPlayerException exception) {
        exception.getActor().error("You must be a player to use this command!");
    }

    protected void senderNotConsole(@NotNull SenderNotConsoleException exception) {
        exception.getActor().error("This command can only be used on console!");
    }

    protected void invalidPlayer(@NotNull InvalidPlayerException exception) {
        exception.getActor().error("Invalid player: &e" + exception.getInput());
    }

    protected void invalidWorld(@NotNull InvalidWorldException exception) {
        exception.getActor().error("Invalid world: &e" + exception.getInput());
    }

    protected void handleUnknownThrowable(@NotNull Throwable throwable) {
        throwable.printStackTrace();
    }
}
