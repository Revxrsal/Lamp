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
package revxrsal.commands.bukkit.brigadier;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.hook.CancelHandle;
import revxrsal.commands.hook.CommandRegisteredHook;

import java.lang.reflect.InvocationTargetException;

import static revxrsal.commands.bukkit.util.BukkitVersion.isPaper;
import static revxrsal.commands.bukkit.util.BukkitVersion.supports;

public final class BrigadierRegistryHook<A extends BukkitCommandActor> implements CommandRegisteredHook<A> {

    private final ActorFactory<A> actorFactory;
    private final ArgumentTypes<A> argumentTypes;
    private final BukkitBrigadierBridge<A> bridge;
    private final JavaPlugin plugin;

    public BrigadierRegistryHook(ArgumentTypes<A> argumentTypes, ActorFactory<A> actorFactory, JavaPlugin plugin) {
        this.actorFactory = actorFactory;
        this.argumentTypes = argumentTypes;
        this.plugin = plugin;
        this.bridge = createBridge();
    }

    private BukkitBrigadierBridge<A> createBridge() {
        if (isPaper()) {
            if (supports(1, 20, 6)) {
                try {
                    //noinspection unchecked
                    return Class.forName("revxrsal.commands.paper.brigadier.registry.ByPaperLifecycle")
                            .asSubclass(BukkitBrigadierBridge.class)
                            .getDeclaredConstructor(JavaPlugin.class, ArgumentTypes.class, ActorFactory.class)
                            .newInstance(plugin, argumentTypes, actorFactory);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException | ClassNotFoundException ignored) {
                }
            }
            if (supports(1, 19)) {
                return new ByPaperEvents<>(plugin, argumentTypes, actorFactory);
            }
        }
        return new ByReflection<>(plugin, argumentTypes, actorFactory);
    }

    @Override
    public void onRegistered(@NotNull ExecutableCommand<A> command, @NotNull CancelHandle cancelHandle) {
        bridge.register(command);
    }
}
