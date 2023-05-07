package revxrsal.commands.jda.core;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.jda.JDAActor;
import revxrsal.commands.process.SenderResolver;

enum JDASenderResolver implements SenderResolver {
    INSTANCE;

    @Override public boolean isCustomType(Class<?> type) {
        return MessageReceivedEvent.class.isAssignableFrom(type)
                || Member.class.isAssignableFrom(type)
                || User.class.isAssignableFrom(type)
                || MessageChannel.class.isAssignableFrom(type);
    }

    @Override public @NotNull Object getSender(@NotNull Class<?> customSenderType, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
        JDAActor jActor = (JDAActor) actor;
        if (MessageReceivedEvent.class.isAssignableFrom(customSenderType)) {
            return jActor.getEvent();
        } else if (Member.class.isAssignableFrom(customSenderType)) {
            return jActor.checkInGuild(command).getMember();
        } else if (User.class.isAssignableFrom(customSenderType)) {
            return jActor.getUser();
        } else if (TextChannel.class.isAssignableFrom(customSenderType)) {
            return jActor.checkInGuild(command).getChannel();
        } else if (PrivateChannel.class.isAssignableFrom(customSenderType)) {
            return jActor.checkNotInGuild(command).getChannel();
        } else if (MessageChannel.class.isAssignableFrom(customSenderType)) {
            return jActor.getChannel();
        }
        return actor;
    }
}
