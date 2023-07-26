package revxrsal.commands.sponge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandPermission;

@Getter
@ToString
@AllArgsConstructor
public final class SpongeCommandPermission implements CommandPermission {

    /**
     * The permission node
     */
    private final @NotNull String permission;

    /**
     * Returns whether the sender has permission to use this command
     * or not.
     *
     * @param actor Actor to test against
     * @return {@code true} if they can use it, false if otherwise.
     */
    @Override public boolean canExecute(@NotNull CommandActor actor) {
        return ((SpongeCommandActor) actor).getSource().hasPermission(permission);
    }
}
