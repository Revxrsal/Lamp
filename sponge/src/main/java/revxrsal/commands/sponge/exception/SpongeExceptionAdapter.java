package revxrsal.commands.sponge.exception;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TextMessageException;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.DefaultExceptionHandler;
import revxrsal.commands.sponge.SpongeCommandActor;

public /*final*/ class SpongeExceptionAdapter extends DefaultExceptionHandler {

    public static final SpongeExceptionAdapter INSTANCE = new SpongeExceptionAdapter();

    @Override protected final void handleUnknown(@NotNull CommandActor actor, @NotNull Throwable throwable) {
        if (throwable instanceof TextMessageException) {
            Text text = ((TextMessageException) throwable).getText();
            if (text != null) actor.as(SpongeCommandActor.class).reply(text);
        }
        if (throwable instanceof SenderNotPlayerException)
            senderNotPlayer(actor, (SenderNotPlayerException) throwable);
        else if (throwable instanceof SenderNotConsoleException)
            senderNotConsole(actor, (SenderNotConsoleException) throwable);
        else if (throwable instanceof InvalidPlayerException)
            invalidPlayer(actor, (InvalidPlayerException) throwable);
        else if (throwable instanceof InvalidWorldException)
            invalidWorld(actor, (InvalidWorldException) throwable);
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

    protected void invalidWorld(@NotNull CommandActor actor, @NotNull InvalidWorldException exception) {
        actor.error("Invalid world: &e" + exception.getInput());
    }

    protected void handleUnknownThrowable(@NotNull CommandActor actor, @NotNull Throwable throwable) {
        throwable.printStackTrace();
    }
}
