package revxrsal.commands.jda.core.actor;

import static revxrsal.commands.jda.core.actor.MemoizingSupplier.memoize;

import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.JDAActor;
import revxrsal.commands.jda.exception.GuildOnlyCommandException;
import revxrsal.commands.jda.exception.PrivateMessageOnlyCommandException;

import java.util.UUID;
import java.util.function.Supplier;

public abstract class BaseActorJDA implements JDAActor {

    private final Supplier<UUID> uuid = memoize(() -> new UUID(0, getUser().getIdLong()));
    private final Event event;
    private final CommandHandler handler;

    public BaseActorJDA(Event event, CommandHandler handler) {
        this.event = event;
        this.handler = handler;
    }

    @Override
    public @NotNull Event getGenericEvent() {
        return event;
    }

    @Override public @NotNull String getName() {
        return getMember().getEffectiveName();
    }

    @Override public @NotNull UUID getUniqueId() {
        return uuid.get();
    }

    @Override public void reply(@NotNull String message) {
        getChannel().sendMessage(handler.getMessagePrefix() + message).queue();
    }

    @Override public void error(@NotNull String message) {
        getChannel().sendMessage(handler.getMessagePrefix() + message).queue();
    }

    @Override public CommandHandler getCommandHandler() {
        return handler;
    }

    @Override public JDAActor checkInGuild(ExecutableCommand command) {
        if (!isGuildEvent())
            throw new GuildOnlyCommandException(command);
        return this;
    }

    @Override public JDAActor checkNotInGuild(ExecutableCommand command) throws PrivateMessageOnlyCommandException {
        if (isGuildEvent())
            throw new PrivateMessageOnlyCommandException(command);
        return this;
    }

    @Override public long getIdLong() {
        return getUser().getIdLong();
    }

    @Override public @NotNull String getId() {
        return getUser().getId();
    }
}
