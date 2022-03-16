package revxrsal.commands.velocity.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.DefaultExceptionHandler;

public class VelocityExceptionAdapter extends DefaultExceptionHandler {

    public static final VelocityExceptionAdapter INSTANCE = new VelocityExceptionAdapter();

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
