package revxrsal.commands.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.command.CommandActor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Command("warp")
public final class Manhunt extends JavaPlugin {

    private final Map<UUID, UUID> targets = new HashMap<>();

    @Override
    public void onEnable() {
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.register(this);
        handler.registerBrigadier();
        Bukkit.getScheduler().runTaskTimer(this, () -> targets.forEach((hunterID, runnerID) -> {
            Player hunter = Bukkit.getPlayer(hunterID);
            if (hunter == null) return;
            Player runner = Bukkit.getPlayer(runnerID);
            if (runner == null) return;
            hunter.setCompassTarget(runner.getLocation());
        }), 2, 2);
//        Commodore commodore = CommodoreProvider.getCommodore(this);
//        LiteralArgumentBuilder<Object> warp = literal("warp")
//                .then(literal("set")
//                        .then(argument("value", IntegerArgumentType.integer(1, 2))
//                                .then(argument("chance", DoubleArgumentType.doubleArg(1)))));
//        commodore.register(warp);
    }

    @Override
    public void onDisable() {
    }

    @Subcommand("move")
    public void move(CommandActor actor, EntitySelector selector) {
        for (Entity entity : selector) {
            actor.reply(entity.getName());
        }
    }

}
