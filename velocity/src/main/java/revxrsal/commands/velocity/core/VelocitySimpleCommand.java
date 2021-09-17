package revxrsal.commands.velocity.core;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.velocity.VelocityCommandActor;

import java.util.List;

final class VelocitySimpleCommand implements SimpleCommand {

    private final VelocityHandler handler;

    public VelocitySimpleCommand(VelocityHandler handler) {
        this.handler = handler;
    }

    @Override public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        ArgumentStack arguments = ArgumentStack.of(invocation.arguments());
        arguments.addFirst(invocation.alias());

        VelocityCommandActor actor = new VelocityActor(source, handler.getServer());
        handler.dispatch(actor, arguments);
    }

    @Override public List<String> suggest(Invocation invocation) {
        ArgumentStack arguments = ArgumentStack.of(invocation.arguments());
        arguments.addFirst(invocation.alias());
        VelocityCommandActor actor = new VelocityActor(invocation.source(), handler.getServer());

        return handler.getAutoCompleter().complete(actor, arguments);
    }
}
