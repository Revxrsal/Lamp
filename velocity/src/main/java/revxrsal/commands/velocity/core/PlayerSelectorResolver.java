package revxrsal.commands.velocity.core;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ValueResolver;
import revxrsal.commands.velocity.PlayerSelector;
import revxrsal.commands.velocity.VelocityCommandActor;
import revxrsal.commands.velocity.exception.InvalidPlayerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

enum PlayerSelectorResolver implements ValueResolver<PlayerSelector> {

    INSTANCE;

    @Override public PlayerSelector resolve(@NotNull ArgumentStack arguments, @NotNull CommandActor actor, @NotNull CommandParameter parameter, @NotNull ExecutableCommand command) {
        VelocityCommandActor subject = (VelocityCommandActor) actor;
        ProxyServer server = subject.getServer();
        String value = arguments.pop().toLowerCase();
        List<Player> coll = new ArrayList<>();
        Player[] players = server.getAllPlayers().toArray(new Player[0]);
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
                Player player = server.getPlayer(value).orElseThrow(() -> new InvalidPlayerException(parameter, value, actor));
                coll.add(player);
                return coll::iterator;
            }
        }
    }
}
