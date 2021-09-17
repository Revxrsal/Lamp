package revxrsal.commands.bungee.core;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.bungee.PlayerSelector;
import revxrsal.commands.bungee.exception.InvalidPlayerException;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ValueResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

enum PlayerSelectorResolver implements ValueResolver<PlayerSelector> {

    INSTANCE;

    @Override public PlayerSelector resolve(@NotNull ArgumentStack arguments, @NotNull CommandActor actor, @NotNull CommandParameter parameter, @NotNull ExecutableCommand command) {
        BungeeCommandActor subject = (BungeeCommandActor) actor;
        String value = arguments.pop().toLowerCase();
        List<ProxiedPlayer> coll = new ArrayList<>();
        ProxiedPlayer[] players = ProxyServer.getInstance().getPlayers().toArray(new ProxiedPlayer[0]);
        switch (value) {
            case "@r":
                coll.add(players[ThreadLocalRandom.current().nextInt(players.length)]);
                return coll::iterator;
            case "@a": {
                Collections.addAll(coll, players);
                return coll::iterator;
            }
            case "@s":
            case "@p": {
                coll.add(subject.requirePlayer());
                return coll::iterator;
            }
            default: {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(value);
                if (player == null)
                    throw new InvalidPlayerException(parameter, value, actor);
                coll.add(player);
                return coll::iterator;
            }
        }
    }
}
