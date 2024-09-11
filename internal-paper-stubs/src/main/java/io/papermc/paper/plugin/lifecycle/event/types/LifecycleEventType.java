package io.papermc.paper.plugin.lifecycle.event.types;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.LifecycleEventHandlerConfiguration;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.MonitorLifecycleEventHandlerConfiguration;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.PrioritizedLifecycleEventHandlerConfiguration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Base type for all types of lifecycle events. Differs from
 * {@link LifecycleEvent} which is the actual event object, whereas
 * this is an object representing the type of the event. Used
 * to construct subtypes of {@link LifecycleEventHandlerConfiguration} for
 * use in {@link LifecycleEventManager}
 *
 * @param <O> the required owner type
 * @param <E> the event object type
 * @param <C> the configuration type
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface LifecycleEventType<O, E extends LifecycleEvent, C extends LifecycleEventHandlerConfiguration<O>> {

    /**
     * Gets the name of the lifecycle event.
     *
     * @return the name
     */
    @Contract(pure = true)
    @NotNull String name();

    /**
     * Create a configuration for this event with the specified
     * handler.
     *
     * @param handler the event handler
     * @return a new configuration
     * @see LifecycleEventManager#registerEventHandler(LifecycleEventHandlerConfiguration)
     */
    @Contract("_ -> new")
    @NotNull C newHandler(@NotNull LifecycleEventHandler<? super E> handler);

    /**
     * Lifecycle event type that supports separate registration
     * of handlers as "monitors" that are run last. Useful
     * if a plugin wants to only observe the changes other handlers
     * made.
     *
     * @param <O> the required owner type
     * @param <E> the event object type
     */
    @ApiStatus.Experimental
    @ApiStatus.NonExtendable
    interface Monitorable<O, E extends LifecycleEvent> extends LifecycleEventType<O, E, MonitorLifecycleEventHandlerConfiguration<O>> {
    }

    /**
     * Lifecycle event type that supports both {@link Monitorable "monitors"} and
     * specific numeric-based priorities.
     *
     * @param <O> the required owner type
     * @param <E> the event object type
     */
    @ApiStatus.Experimental
    @ApiStatus.NonExtendable
    interface Prioritizable<O, E extends LifecycleEvent> extends LifecycleEventType<O, E, PrioritizedLifecycleEventHandlerConfiguration<O>> {
    }
}
