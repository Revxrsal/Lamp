package revxrsal.commands.velocity.core;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
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

    @Override public PlayerSelector resolve(@NotNull ValueResolverContext context) {
        VelocityCommandActor vActor = context.actor();
        ProxyServer server = vActor.getServer();
        String value = context.pop().toLowerCase();
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
                coll.add(vActor.requirePlayer());
                return coll::iterator;
            }
            default: {
                Player player = server.getPlayer(value).orElseThrow(() -> new InvalidPlayerException(context.parameter(), value));
                coll.add(player);
                return coll::iterator;
            }
        }
    }
}
