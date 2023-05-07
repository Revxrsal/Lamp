package revxrsal.commands.jda;

import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.trait.CommandAnnotationHolder;
import revxrsal.commands.jda.annotation.GuildPermission;
import revxrsal.commands.jda.annotation.RolePermission;
import revxrsal.commands.jda.annotation.UserPermission;

@Getter
public final class JDAPermission implements CommandPermission {

    private final @Nullable RolePermission roles;
    private final @Nullable GuildPermission permissions;
    private final @Nullable UserPermission users;

    public JDAPermission(CommandAnnotationHolder command) {
        roles = command.getAnnotation(RolePermission.class);
        permissions = command.getAnnotation(GuildPermission.class);
        users = command.getAnnotation(UserPermission.class);
    }

    @Override public boolean canExecute(@NotNull CommandActor actor) {
        if (roles == null && permissions == null && users == null)
            return true;
        JDAActor jActor = (JDAActor) actor;
        if (jActor.isGuildEvent()) {
            if (jActor.getMember().isOwner())
                return true;
            if (roles != null) { // check roles
                for (String name : roles.names())
                    if (jActor.getMember().getRoles().stream().anyMatch(c -> c.getName().equalsIgnoreCase(name)))
                        return true;
                for (long id : roles.ids())
                    if (jActor.getMember().getRoles().stream().anyMatch(c -> c.getIdLong() == id))
                        return true;
            }
            if (permissions != null && jActor.isGuildEvent())
                return jActor.getMember().hasPermission((GuildChannel) jActor.getChannel(), permissions.value());
        }
        if (users != null) {
            for (String allowed : users.names())
                if (actor.getName().equalsIgnoreCase(allowed))
                    return true;
            for (long allowed : users.ids())
                if (jActor.getUser().getIdLong() == allowed)
                    return true;
        }
        return false;
    }
}
