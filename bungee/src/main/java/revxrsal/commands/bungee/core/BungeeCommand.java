package revxrsal.commands.bungee.core;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.command.ArgumentStack;

final class BungeeCommand extends Command implements TabExecutor {

    private final BungeeHandler handler;

    public BungeeCommand(String name, BungeeHandler handler) {
        super(name);
        this.handler = handler;
    }

    @Override public void execute(CommandSender sender, String[] args) {
        BungeeCommandActor actor = new BungeeActor(sender, handler);
        try {
            ArgumentStack arguments = ArgumentStack.of(args);
            arguments.addFirst(getName());

            handler.dispatch(actor, arguments);
        } catch (Throwable t) {
            handler.getExceptionHandler().handleException(t, actor);
        }
    }

    @Override public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        ArgumentStack arguments = ArgumentStack.forAutoCompletion(args);
        arguments.addFirst(getName());

        BungeeCommandActor actor = new BungeeActor(sender, handler);
        return handler.getAutoCompleter().complete(actor, arguments);
    }
}
