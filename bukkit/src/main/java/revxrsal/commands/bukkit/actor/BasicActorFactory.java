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

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.util.Optional;

/**
 * Default implementation of {@link ActorFactory}
 */
final class BasicActorFactory implements ActorFactory<BukkitCommandActor> {

    private final Plugin plugin;
    private final Optional<BukkitAudiences> bukkitAudiences;
    private final MessageSender<BukkitCommandActor, ComponentLike> messageSender;
    private final MessageSender<BukkitCommandActor, ComponentLike> errorSender;

    public BasicActorFactory(Plugin plugin, Optional<BukkitAudiences> bukkitAudiences) {
        this(plugin, bukkitAudiences, null, null);
    }

    public BasicActorFactory(
            Plugin plugin,
            Optional<BukkitAudiences> bukkitAudiences,
            MessageSender<BukkitCommandActor, ComponentLike> messageSender,
            MessageSender<BukkitCommandActor, ComponentLike> errorSender
    ) {
        this.plugin = plugin;
        this.bukkitAudiences = bukkitAudiences;
        this.messageSender = messageSender;
        this.errorSender = errorSender;
    }

    @Override
    public @NotNull BukkitCommandActor create(@NotNull CommandSender sender, @NotNull Lamp<BukkitCommandActor> lamp) {
        return new BasicBukkitActor(sender, plugin, bukkitAudiences, messageSender, errorSender, lamp);
    }
}
