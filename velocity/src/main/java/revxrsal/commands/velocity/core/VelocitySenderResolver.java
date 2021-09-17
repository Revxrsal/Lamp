package revxrsal.commands.velocity.core;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.SenderResolver;
import revxrsal.commands.velocity.VelocityCommandActor;

enum VelocitySenderResolver implements SenderResolver {
    INSTANCE;

    @Override public boolean isCustomType(Class<?> type) {
        return CommandSource.class.isAssignableFrom(type);
    }

    @Override public @NotNull Object getSender(@NotNull Class<?> customSenderType, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
        if (Player.class.isAssignableFrom(customSenderType)) {
            return ((VelocityCommandActor) actor).requirePlayer();
        }
        return ((VelocityCommandActor) actor).getSource();
    }
}
