package revxrsal.commands.bukkit.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.DefaultExceptionHandler;

public class BukkitExceptionAdapter extends DefaultExceptionHandler {

    public static final BukkitExceptionAdapter INSTANCE = new BukkitExceptionAdapter();

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

    public void malformedEntitySelector(@NotNull CommandActor actor, @NotNull MalformedEntitySelectorException exception) {
        actor.errorLocalized("invalid-selector", exception.getInput());
    }

    public void invalidNamespacedKey(@NotNull final CommandActor actor, @NotNull InvalidNamespacedKeyException exception) {
        actor.errorLocalized("invalid-namespacedkey", exception.getInput());
    }
}
