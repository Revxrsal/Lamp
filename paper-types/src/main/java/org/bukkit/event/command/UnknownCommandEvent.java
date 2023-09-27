package org.bukkit.event.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnknownCommandEvent extends Event {

    /**
     * Gets the CommandSender or ConsoleCommandSender
     * <p>
     *
     * @return Sender of the command
     */
    @NotNull
    public CommandSender getSender() {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * Gets the command that was send
     * <p>
     *
     * @return Command sent
     */
    @NotNull
    public String getCommandLine() {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * Gets message that will be returned
     * <p>
     *
     * @return Unknown command message
     * @deprecated use {@link #message()}
     */
    @Nullable
    @Deprecated
    public String getMessage() {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * Sets message that will be returned
     * <p>
     * Set to null to avoid any message being sent
     *
     * @param message the message to be returned, or null
     * @deprecated use {@link #message(Component)}
     */
    @Deprecated
    public void setMessage(@Nullable String message) {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * Gets message that will be returned
     * <p>
     *
     * @return Unknown command message
     */
    @Nullable
    @Contract(pure = true)
    public Component message() {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * Sets message that will be returned
     * <p>
     * Set to null to avoid any message being sent
     *
     * @param message the message to be returned, or null
     */
    public void message(@Nullable Component message) {
        throw new UnsupportedOperationException("Stub");
    }

    @NotNull
    public HandlerList getHandlers() {
        throw new UnsupportedOperationException("Stub");
    }

    @NotNull
    public static HandlerList getHandlerList() {
        throw new UnsupportedOperationException("Stub");
    }
}
