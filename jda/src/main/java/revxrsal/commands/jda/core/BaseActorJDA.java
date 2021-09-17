package revxrsal.commands.jda.core;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.JDAActor;
import revxrsal.commands.jda.exception.GuildOnlyCommandException;
import revxrsal.commands.jda.exception.PrivateMessageOnlyCommandException;

import java.util.UUID;
import java.util.function.Supplier;

import static revxrsal.commands.jda.core.MemoizingSupplier.memoize;
import static revxrsal.commands.util.Preconditions.notNull;

final class BaseActorJDA implements JDAActor {

    private final Supplier<UUID> uuid = memoize(() -> new UUID(0, getUser().getIdLong()));
    private final MessageReceivedEvent event;

    public BaseActorJDA(MessageReceivedEvent event) {
        this.event = event;
    }

    @Override public @NotNull String getName() {
        return getMember().getEffectiveName();
    }

    @Override public @NotNull UUID getUniqueId() {
        return uuid.get();
    }

    @Override public void reply(@NotNull String message) {
        getChannel().sendMessage(message).queue();
    }

    @Override public void error(@NotNull String message) {
        getChannel().sendMessage(message).queue();
    }

    @Override public @NotNull Member getMember() {
        return notNull(event.getMember(), "event.getMember()");
    }

    @Override public JDAActor checkInGuild(ExecutableCommand command) {
        if (!isGuildEvent())
            throw new GuildOnlyCommandException(this, command);
        return this;
    }

    @Override public JDAActor checkNotInGuild(ExecutableCommand command) throws PrivateMessageOnlyCommandException {
        if (isGuildEvent())
            throw new PrivateMessageOnlyCommandException(this, command);
        return this;
    }

    @Override public long getIdLong() {
        return getUser().getIdLong();
    }

    @Override public @NotNull String getId() {
        return getUser().getId();
    }

    @Override public @NotNull User getUser() {
        return event.getAuthor();
    }

    @Override public @NotNull Message getMessage() {
        return event.getMessage();
    }

    @Override public @NotNull MessageReceivedEvent getEvent() {
        return event;
    }

    @Override public @NotNull Guild getGuild() {
        return event.getGuild();
    }

    @Override public @NotNull MessageChannel getChannel() {
        return event.getChannel();
    }

    @Override public boolean isGuildEvent() {
        return event.isFromGuild();
    }
}
