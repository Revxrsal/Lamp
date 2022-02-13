package revxrsal.commands.sponge.exception;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.util.TextMessageException;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.DefaultExceptionHandler;
import revxrsal.commands.sponge.SpongeCommandActor;

public class SpongeExceptionAdapter extends DefaultExceptionHandler {

    public static final SpongeExceptionAdapter INSTANCE = new SpongeExceptionAdapter();

    public void onTextMessage(@NotNull SpongeCommandActor actor, @NotNull TextMessageException e) {
        if (e.getText() != null) actor.reply(e.getText());
    }

    public void senderNotPlayer(@NotNull CommandActor actor, @NotNull SenderNotPlayerException exception) {
        actor.error("You must be a player to use this command!");
    }

    public void senderNotConsole(@NotNull CommandActor actor, @NotNull SenderNotConsoleException exception) {
        actor.error("This command can only be used on console!");
    }

    public void invalidPlayer(@NotNull CommandActor actor, @NotNull InvalidPlayerException exception) {
        actor.error("Invalid player: &e" + exception.getInput());
    }

    public void invalidWorld(@NotNull CommandActor actor, @NotNull InvalidWorldException exception) {
        actor.error("Invalid world: &e" + exception.getInput());
    }
}
