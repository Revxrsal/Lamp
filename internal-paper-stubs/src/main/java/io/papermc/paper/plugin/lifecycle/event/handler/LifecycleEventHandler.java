package io.papermc.paper.plugin.lifecycle.event.handler;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A handler for a specific event. Can be implemented
 * in a concrete class or as a lambda.
 *
 * @param <E> the event
 */
@ApiStatus.Experimental
@FunctionalInterface
public interface LifecycleEventHandler<E extends LifecycleEvent> {

    void run(@NotNull E event);
}
