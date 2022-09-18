package dev.demeng.pluginbase.commands.bukkit.exception;

import dev.demeng.pluginbase.commands.exception.ThrowableFromCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Thrown when a player-only command is executed by a non-player
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class SenderNotPlayerException extends RuntimeException {

}
