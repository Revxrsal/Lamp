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

    private final @Nullable RolePermission rp;
    private final @Nullable GuildPermission gp;
    private final @Nullable UserPermission up;

    public JDAPermission(CommandAnnotationHolder command) {
        rp = command.getAnnotation(RolePermission.class);
        gp = command.getAnnotation(GuildPermission.class);
        up = command.getAnnotation(UserPermission.class);
    }

    @Override public boolean canExecute(@NotNull CommandActor actor) {
        if (rp == null && gp == null && up == null)
            return true;
        JDAActor jActor = (JDAActor) actor;
        if (jActor.isGuildEvent()) {
            if (jActor.getMember().isOwner())
                return true;
            if (rp != null) { // check roles
                for (String name : rp.names())
                    if (jActor.getMember().getRoles().stream().anyMatch(c -> c.getName().equalsIgnoreCase(name)))
                        return true;
                for (long id : rp.ids())
                    if (jActor.getMember().getRoles().stream().anyMatch(c -> c.getIdLong() == id))
                        return true;
            }
            if (gp != null && jActor.isGuildEvent())
                return jActor.getMember().hasPermission((GuildChannel) jActor.getChannel(), gp.value());
        }
        if (up != null) {
            for (String allowed : up.names())
                if (actor.getName().equalsIgnoreCase(allowed))
                    return true;
            for (long allowed : up.ids())
                if (jActor.getUser().getIdLong() == allowed)
                    return true;
        }
        return false;
    }
}
