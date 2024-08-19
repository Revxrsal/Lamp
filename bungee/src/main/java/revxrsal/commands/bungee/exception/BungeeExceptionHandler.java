package revxrsal.commands.bungee.exception;

import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.exception.DefaultExceptionHandler;

import static revxrsal.commands.bungee.util.BungeeUtils.legacyColorize;

public class BungeeExceptionHandler extends DefaultExceptionHandler<BungeeCommandActor> {

    @HandleException
    public void onInvalidPlayer(InvalidPlayerException e, BungeeCommandActor actor) {
        actor.error(legacyColorize("&cInvalid player: &e" + e.input() + "&c."));
    }

    @HandleException
    public void onSenderNotPlayer(SenderNotPlayerException e, BungeeCommandActor actor) {
        actor.error(legacyColorize("&cYou must be a player to execute this command!"));
    }
}
