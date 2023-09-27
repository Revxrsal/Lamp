package com.destroystokyo.paper.event.brigadier;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.command.Command;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.ServerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Fired anytime the server synchronizes Bukkit commands to Brigadier.
 *
 * <p>Allows a plugin to control the command node structure for its commands.
 * This is done at Plugin Enable time after commands have been registered, but may also
 * run at a later point in the server lifetime due to plugins, a server reload, etc.</p>
 *
 * <p>This is a draft/experimental API and is subject to change.</p>
 */
@ApiStatus.Experimental
public class CommandRegisteredEvent<S> extends ServerEvent implements Cancellable {

    /**
     * Gets the command label of the {@link Command} being registered.
     *
     * @return the command label
     */
    public String getCommandLabel() {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * Gets the {@link Command} being registered.
     *
     * @return the {@link Command}
     */
    public Command getCommand() {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * Gets the {@link RootCommandNode} which is being registered to.
     *
     * @return the {@link RootCommandNode}
     */
    public RootCommandNode<S> getRoot() {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * Gets the Bukkit APIs default arguments node (greedy string), for if
     * you wish to reuse it.
     *
     * @return default arguments node
     */
    public ArgumentCommandNode<S, String> getDefaultArgs() {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * Gets the {@link LiteralCommandNode} to be registered for the {@link Command}.
     *
     * @return the {@link LiteralCommandNode}
     */
    public LiteralCommandNode<S> getLiteral() {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * Sets the {@link LiteralCommandNode} used to register this command. The default literal is mutable, so
     * this is primarily if you want to completely replace the object.
     *
     * @param literal new node
     */
    public void setLiteral(LiteralCommandNode<S> literal) {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * Gets whether this command should is treated as "raw".
     *
     * @return whether this command is treated as "raw"
     * @see #setRawCommand(boolean)
     */
    public boolean isRawCommand() {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * Sets whether this command should be treated as "raw".
     *
     * <p>A "raw" command will only use the node provided by this event for
     * sending the command tree to the client. For execution purposes, the default
     * greedy string execution of a standard Bukkit {@link Command} is used.</p>
     *
     * <p>On older versions of Paper, this was the default and only behavior of this
     * event.</p>
     *
     * @param rawCommand whether this command should be treated as "raw"
     */
    public void setRawCommand(final boolean rawCommand) {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException("Stub");
    }

    /**
     * Cancels registering this command to Brigadier, but will remain in Bukkit Command Map. Can be used to hide a
     * command from all players.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void setCancelled(boolean cancel) {
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