package revxrsal.commands.bukkit.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.command.ArgumentStack;

import java.util.List;

final class BukkitCommandExecutor implements TabExecutor {

    private final BukkitHandler handler;

    public BukkitCommandExecutor(BukkitHandler handler) {
        this.handler = handler;
    }

    @Override public boolean onCommand(@NotNull CommandSender sender,
                                       @NotNull Command command,
                                       @NotNull String label,
                                       @NotNull String[] args) {
        ArgumentStack arguments = ArgumentStack.of(args);
        arguments.addFirst(command.getName());

        BukkitCommandActor actor = new BukkitActor(sender, handler);
        handler.dispatch(actor, arguments);
        return true;
    }

    @Nullable @Override public List<String> onTabComplete(@NotNull CommandSender sender,
                                                          @NotNull Command command,
                                                          @NotNull String alias,
                                                          @NotNull String[] args) {
        BukkitCommandActor actor = new BukkitActor(sender, handler);
        ArgumentStack arguments = ArgumentStack.forAutoCompletion(args);

        arguments.addFirst(command.getName());
        return handler.getAutoCompleter().complete(actor, arguments);
    }
}
