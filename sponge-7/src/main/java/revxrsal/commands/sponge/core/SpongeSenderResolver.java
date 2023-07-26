package revxrsal.commands.sponge.core;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.SenderResolver;
import revxrsal.commands.sponge.SpongeCommandActor;

enum SpongeSenderResolver implements SenderResolver {
    INSTANCE;

    @Override public boolean isCustomType(Class<?> type) {
        return CommandSource.class.isAssignableFrom(type);
    }

    @Override public @NotNull Object getSender(@NotNull Class<?> customSenderType, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
        SpongeCommandActor sActor = actor.as(SpongeCommandActor.class);
        if (Player.class.isAssignableFrom(customSenderType))
            return sActor.requirePlayer();
        if (ConsoleSource.class.isAssignableFrom(customSenderType))
            return sActor.requireConsole();
        return actor;
    }
}
