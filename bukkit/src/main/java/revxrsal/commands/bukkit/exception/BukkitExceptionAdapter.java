package revxrsal.commands.bukkit.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.DefaultExceptionHandler;

public class BukkitExceptionAdapter extends DefaultExceptionHandler {

    public static final BukkitExceptionAdapter INSTANCE = new BukkitExceptionAdapter();

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

    public void malformedEntitySelector(@NotNull CommandActor actor, @NotNull MalformedEntitySelectorException exception) {
        actor.error("Invalid selector argument: &e" + exception.getInput());
    }
}
