/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.sponge.actor;

import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandCause;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

/**
 * Represents a functional interface that allows for creating custom
 * implementations of {@link SpongeCommandActor} that wrap instances
 * of {@link CommandCause}.
 *
 * @param <A> The actor type
 */
@FunctionalInterface
public interface ActorFactory<A extends SpongeCommandActor> {

    /**
     * Returns the default {@link ActorFactory} that returns a simple {@link SpongeCommandActor}
     * implementation
     *
     * @return The default {@link ActorFactory}.
     */
    static @NotNull ActorFactory<SpongeCommandActor> defaultFactory() {
        return BasicActorFactory.INSTANCE;
    }

    /**
     * Returns the default {@link ActorFactory} that returns a simple {@link SpongeCommandActor}
     * implementation, with custom {@link MessageSender}s for messages and errors
     *
     * @return The default {@link ActorFactory}.
     */
    static @NotNull ActorFactory<SpongeCommandActor> defaultFactory(
            @NotNull MessageSender<SpongeCommandActor, ComponentLike> messageSender,
            @NotNull MessageSender<SpongeCommandActor, ComponentLike> errorSender
    ) {
        return new BasicActorFactory(messageSender, errorSender);
    }

    /**
     * Creates the actor from the given {@link CommandCause}
     *
     * @param sender Sender to create for
     * @param lamp   The {@link Lamp} instance
     * @return The created actor
     */
    @NotNull A create(@NotNull CommandCause sender, @NotNull Lamp<A> lamp);
}
