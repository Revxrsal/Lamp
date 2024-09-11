package io.papermc.paper.plugin.bootstrap;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A plugin bootstrap is meant for loading certain parts of the plugin before the server is loaded.
 * <p>
 * Plugin bootstrapping allows values to be initialized in certain parts of the server that might not be allowed
 * when the server is running.
 * <p>
 * Your bootstrap class will be on the same classloader as your JavaPlugin.
 * <p>
 * <b>All calls to Bukkit may throw a NullPointerExceptions or return null unexpectedly. You should only call api methods that are explicitly documented to work in the bootstrapper</b>
 */
@ApiStatus.OverrideOnly
@ApiStatus.Experimental
public interface PluginBootstrap {

    /**
     * Called by the server, allowing you to bootstrap the plugin with a context that provides things like a logger and your shared plugin configuration file.
     *
     * @param context the server provided context
     */
    void bootstrap(@NotNull BootstrapContext context);

}
