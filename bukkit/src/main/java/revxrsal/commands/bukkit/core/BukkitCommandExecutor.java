package revxrsal.commands.bukkit.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.exception.ArgumentParseException;

import java.util.Collections;
import java.util.List;

import static revxrsal.commands.util.Strings.stripNamespace;

public final class BukkitCommandExecutor implements TabExecutor {

    private final BukkitHandler handler;

    public BukkitCommandExecutor(BukkitHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        BukkitCommandActor actor = new BukkitActor(sender, handler);
        try {
            ArgumentStack arguments = ArgumentStack.parse(args);
            arguments.addFirst(stripNamespace(command.getName()));

            handler.dispatch(actor, arguments);
        } catch (Throwable t) {
            handler.getExceptionHandler().handleException(t, actor);
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        try {
            BukkitCommandActor actor = new BukkitActor(sender, handler);
            ArgumentStack arguments = ArgumentStack.parseForAutoCompletion(args);

            arguments.addFirst(stripNamespace(command.getName()));
            return handler.getAutoCompleter().complete(actor, arguments);
        } catch (ArgumentParseException e) {
            return Collections.emptyList();
        }
    }
}
