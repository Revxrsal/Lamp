package revxrsal.commands.sponge.exception;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.util.ComponentMessageException;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.DefaultExceptionHandler;
import revxrsal.commands.sponge.SpongeCommandActor;

public class SpongeExceptionAdapter extends DefaultExceptionHandler {

    public static final SpongeExceptionAdapter INSTANCE = new SpongeExceptionAdapter();

    public void onTextMessage(@NotNull SpongeCommandActor actor, @NotNull ComponentMessageException e) {
        if(e.componentMessage() != null) actor.reply(e.componentMessage());
    }

    public void senderNotPlayer(@NotNull CommandActor actor, @NotNull SenderNotPlayerException exception) {
        actor.errorLocalized("must-be-player");
    }

    public void senderNotConsole(@NotNull CommandActor actor, @NotNull SenderNotConsoleException exception) {
        actor.errorLocalized("must-be-console");
    }

    public void invalidPlayer(@NotNull CommandActor actor, @NotNull InvalidPlayerException exception) {
        actor.errorLocalized("invalid-player", exception.getInput());
    }

    public void invalidWorld(@NotNull CommandActor actor, @NotNull InvalidWorldException exception) {
        actor.errorLocalized("invalid-world", exception.getInput());
    }
}
