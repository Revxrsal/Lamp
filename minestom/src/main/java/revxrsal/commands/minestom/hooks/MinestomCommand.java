package revxrsal.commands.minestom.hooks;

import net.minestom.server.command.builder.Command;
import revxrsal.commands.Lamp;
import revxrsal.commands.minestom.actor.ActorFactory;
import revxrsal.commands.minestom.actor.MinestomCommandActor;

final class MinestomCommand<A extends MinestomCommandActor> extends Command {

    private final Lamp<A> lamp;
    private final ActorFactory<A> actorFactory;

    public MinestomCommand(String name, Lamp<A> lamp, ActorFactory<A> actorFactory) {
        super(name);
        this.lamp = lamp;
        this.actorFactory = actorFactory;
    }

    private static String ignoreAfterSpace(String v) {
        int spaceIndex = v.indexOf(' ');
        return spaceIndex == -1 ? v : v.substring(0, spaceIndex);
    }

//    public void execute(CommandSender sender, String[] args) {
//        A actor = actorFactory.create(sender, lamp);
//        MutableStringStream input = createInput(getName(), args);
//        lamp.dispatch(actor, input);
//    }
//
//    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
//        A actor = actorFactory.create(sender, lamp);
//        MutableStringStream input = createInput(getName(), args);
//        List<String> completions = lamp.autoCompleter().complete(actor, input);
//         on older versions, we get funny behavior when suggestions contain spaces
//        return map(completions, MinestomCommand::ignoreAfterSpace);
//    }
}
