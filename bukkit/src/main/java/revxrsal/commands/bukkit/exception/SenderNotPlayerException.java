package revxrsal.commands.bukkit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitCommandActor;

/**
 * Thrown when a player-only command is executed by a non-player
 */
@Getter
@AllArgsConstructor
public class SenderNotPlayerException extends RuntimeException {

    /**
     * The command actor that failed to be a player.
     */
    private final @NotNull BukkitCommandActor actor;

}
