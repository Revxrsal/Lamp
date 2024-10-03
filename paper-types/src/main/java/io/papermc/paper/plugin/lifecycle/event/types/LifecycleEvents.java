package io.papermc.paper.plugin.lifecycle.event.types;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Holds various types of lifecycle events for
 * use when creating event handler configurations
 * in {@link LifecycleEventManager}.
 */
@ApiStatus.Experimental
public final class LifecycleEvents {

    /**
     * This event is for registering commands to the server's brigadier command system. You can register a handler for this event in
     * {@link org.bukkit.plugin.java.JavaPlugin#onEnable()} or {@link io.papermc.paper.plugin.bootstrap.PluginBootstrap#bootstrap(BootstrapContext)}.
     *
     * @see Commands an example of a command being registered
     */
    public static final LifecycleEventType.Prioritizable<LifecycleEventOwner, ReloadableRegistrarEvent<Commands>> COMMANDS = prioritized("commands", LifecycleEventOwner.class);

    @ApiStatus.Internal
    private static <O, E extends LifecycleEvent> LifecycleEventType.Prioritizable<O, E> prioritized(final String name, final Class<? extends O> ownerType) {
        throw new UnsupportedOperationException("Stub");
    }
    //</editor-fold>

    private LifecycleEvents() {
    }
}
