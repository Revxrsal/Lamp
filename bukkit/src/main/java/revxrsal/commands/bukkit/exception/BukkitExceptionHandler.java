package revxrsal.commands.bukkit.exception;

import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.exception.DefaultExceptionHandler;

import static revxrsal.commands.bukkit.util.BukkitUtils.legacyColorize;

public class BukkitExceptionHandler extends DefaultExceptionHandler<BukkitCommandActor> {

    @HandleException
    public void onInvalidPlayer(InvalidPlayerException e, BukkitCommandActor actor) {
        actor.error(legacyColorize("&cInvalid player: &e" + e.input() + "&c."));
    }

    @HandleException
    public void onInvalidPlayer(InvalidWorldException e, BukkitCommandActor actor) {
        actor.error(legacyColorize("&cInvalid world: &e" + e.input() + "&c."));
    }

    @HandleException
    public void onSenderNotConsole(SenderNotConsoleException e, BukkitCommandActor actor) {
        actor.error(legacyColorize("&cYou must be the console to execute this command!"));
    }

    @HandleException
    public void onSenderNotPlayer(SenderNotPlayerException e, BukkitCommandActor actor) {
        actor.error(legacyColorize("&cYou must be a player to execute this command!"));
    }

    @HandleException
    public void onMalformedEntitySelector(MalformedEntitySelectorException e, BukkitCommandActor actor) {
        actor.error(legacyColorize("&cMalformed entity selector: &e" + e.input() + "&c. Error: &e" + e.errorMessage()));
    }

    @HandleException
    public void onNonPlayerEntities(NonPlayerEntitiesException e, BukkitCommandActor actor) {
        actor.error(legacyColorize("&cYour entity selector (&e" + e.input() + "&c) only allows players, but it contains non-player entities too."));
    }

}
