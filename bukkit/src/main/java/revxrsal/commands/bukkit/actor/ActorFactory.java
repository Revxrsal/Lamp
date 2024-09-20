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
package revxrsal.commands.bukkit.actor;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.util.Optional;

/**
 * Represents a functional interface that allows for creating custom
 * implementations of {@link BukkitCommandActor} that wrap instances
 * of {@link CommandSender}.
 *
 * @param <A> The actor type
 */
@FunctionalInterface
public interface ActorFactory<A extends BukkitCommandActor> {

    /**
     * Returns the default {@link ActorFactory} that returns a
     * simple {@link BukkitCommandActor} implementation
     *
     * @param plugin    The plugin to create for
     * @param audiences The {@link BukkitAudiences} instance for
     *                  consrtucting {@link Audience} objects
     * @return The default {@link ActorFactory}.
     */
    static @NotNull ActorFactory<BukkitCommandActor> defaultFactory(
            @NotNull Plugin plugin,
            @NotNull Optional<BukkitAudiences> audiences
    ) {
        return new BasicActorFactory(plugin, audiences);
    }

    /**
     * Returns the default {@link ActorFactory} that returns a
     * simple {@link BukkitCommandActor} implementation
     *
     * @param plugin        The plugin to create for
     * @param audiences     The {@link BukkitAudiences} instance for
     *                      consrtucting {@link Audience} objects
     * @param messageSender How components are sent. This can be used
     *                      to add custom prefixes to messages, etc.
     * @return The default {@link ActorFactory}.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    static <A extends BukkitCommandActor> Object defaultFactory(
            @NotNull JavaPlugin plugin,
            @NotNull Optional<BukkitAudiences> audiences,
            @Nullable MessageSender<? super A, ComponentLike> messageSender
    ) {
        return new BasicActorFactory(plugin, audiences, (MessageSender) messageSender);
    }

    /**
     * Creates the actor from the given {@link CommandSender}
     *
     * @param sender Sender to create for
     * @param lamp   The {@link Lamp} instance
     * @return The created actor
     */
    @NotNull A create(@NotNull CommandSender sender, @NotNull Lamp<A> lamp);
}
