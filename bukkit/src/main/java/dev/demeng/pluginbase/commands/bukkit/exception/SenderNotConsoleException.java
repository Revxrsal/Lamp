package dev.demeng.pluginbase.commands.bukkit.exception;

import dev.demeng.pluginbase.commands.exception.ThrowableFromCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Thrown when a console-only command is executed by a non-console
 */
@Getter
@AllArgsConstructor
@ThrowableFromCommand
public class SenderNotConsoleException extends RuntimeException {

}
