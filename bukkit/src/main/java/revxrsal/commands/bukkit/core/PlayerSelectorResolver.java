package revxrsal.commands.bukkit.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.PlayerSelector;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
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

    @Override public PlayerSelector resolve(@NotNull ValueResolverContext context) {
        BukkitCommandActor bActor = context.actor();
        String value = context.pop().toLowerCase();
        List<Player> coll = new ArrayList<>();
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
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
                coll.add(bActor.requirePlayer());
                return coll::iterator;
            }
            default: {
                Player player = Bukkit.getPlayer(value);
                if (player == null)
                    throw new InvalidPlayerException(context.parameter(), value);
                coll.add(player);
                return coll::iterator;
            }
        }
    }
}
