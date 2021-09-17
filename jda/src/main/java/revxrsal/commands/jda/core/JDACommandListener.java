package revxrsal.commands.jda.core;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.jda.JDACommandHandler;

@AllArgsConstructor final class JDACommandListener implements EventListener {

    private final String prefix;
    private final JDACommandHandler handler;

    @Override public void onEvent(@NotNull GenericEvent genericEvent) {
        if (!(genericEvent instanceof MessageReceivedEvent)) return;
        MessageReceivedEvent event = (MessageReceivedEvent) genericEvent;
        if (event.isWebhookMessage()) return;
        String content = event.getMessage().getContentRaw();
        if (!content.startsWith(prefix)) return;
        content = content.substring(prefix.length());

        ArgumentStack arguments = ArgumentStack.fromString(content);
        CommandActor actor = new BaseActorJDA(event);
        handler.dispatch(actor, arguments);
    }
}
