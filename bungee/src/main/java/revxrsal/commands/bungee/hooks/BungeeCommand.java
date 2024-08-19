package revxrsal.commands.bungee.hooks;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.bungee.BungeeCommandActor;
import revxrsal.commands.bungee.actor.ActorFactory;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.List;
import java.util.StringJoiner;

import static revxrsal.commands.util.Collections.map;
import static revxrsal.commands.util.Strings.stripNamespace;

final class BungeeCommand<A extends BungeeCommandActor> extends Command implements TabExecutor {

    private final Lamp<A> lamp;
    private final ActorFactory<A> actorFactory;

    public BungeeCommand(String name, Lamp<A> lamp, ActorFactory<A> actorFactory) {
        super(name);
        this.lamp = lamp;
        this.actorFactory = actorFactory;
    }

    @Override public void execute(CommandSender sender, String[] args) {
        A actor = actorFactory.create(sender, lamp);

        MutableStringStream input = createInput(getName(), args);
        lamp.dispatch(actor, input);
    }

    @Override public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        A actor = actorFactory.create(sender, lamp);
        MutableStringStream input = createInput(getName(), args);
        List<String> completions = lamp.autoCompleter().complete(actor, input);
        // on older versions, we get funny behavior when suggestions contain spaces
        return map(completions, BungeeCommand::ignoreAfterSpace);
    }

    private static String ignoreAfterSpace(String v) {
        int spaceIndex = v.indexOf(' ');
        return spaceIndex == -1 ? v : v.substring(0, spaceIndex);
    }

    private static @NotNull MutableStringStream createInput(String commandName, String[] args) {
        StringJoiner userInput = new StringJoiner(" ");
        userInput.add(stripNamespace(commandName));
        for (@NotNull String arg : args)
            userInput.add(arg);
        return StringStream.createMutable(userInput.toString());
    }
}
