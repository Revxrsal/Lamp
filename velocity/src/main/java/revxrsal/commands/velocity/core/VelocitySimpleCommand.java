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
        VelocityCommandActor actor = new VelocityActor(source, handler.getServer(), handler);
        try {
            ArgumentStack arguments = ArgumentStack.of(invocation.arguments());
            arguments.addFirst(invocation.alias());

            handler.dispatch(actor, arguments);
        } catch (Throwable t) {
            handler.getExceptionHandler().handleException(t, actor);
        }

    }

    @Override public List<String> suggest(Invocation invocation) {
        VelocityCommandActor actor = new VelocityActor(invocation.source(), handler.getServer(), handler);
        ArgumentStack arguments;
        if (invocation.arguments().length == 0)
            arguments = ArgumentStack.forAutoCompletion("");
        else
            arguments = ArgumentStack.forAutoCompletion(invocation.arguments());
        arguments.addFirst(invocation.alias());
        return handler.getAutoCompleter().complete(actor, arguments);
    }
}
