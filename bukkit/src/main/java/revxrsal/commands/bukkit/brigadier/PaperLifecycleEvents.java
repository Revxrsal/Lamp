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

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.lang.reflect.Method;

final class PaperLifecycleEvents extends Commodore {

    private final RootCommandNode<CommandSourceStack> root = new RootCommandNode<>();
    private final BukkitCommandHandler handler;

    public PaperLifecycleEvents(@NotNull BukkitCommandHandler handler) {
        this.handler = handler;
        getLifecycleManager(handler.getPlugin()).registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            for (CommandNode<CommandSourceStack> node : root.getChildren()) {
                event.registrar().register(((LiteralCommandNode<CommandSourceStack>) node));
            }
        });
    }

    @Override void register(Command command, LiteralCommandNode<?> node) {
        root.addChild((LiteralCommandNode<CommandSourceStack>) node);
    }

    @Override void register(LiteralCommandNode<?> node) {
        root.addChild((LiteralCommandNode<CommandSourceStack>) node);
    }

    private static final Method GET_LIFECYCLE_MANAGER;

    static {
        Method getLifecycleManager;
        try {
            getLifecycleManager = Plugin.class.getDeclaredMethod("getLifecycleManager");
        } catch (NoSuchMethodException e) {
            getLifecycleManager = null;
        }
        GET_LIFECYCLE_MANAGER = getLifecycleManager;
    }

    @SneakyThrows private static LifecycleEventManager<Plugin> getLifecycleManager(Plugin plugin) {
        if (GET_LIFECYCLE_MANAGER == null)
            throw new IllegalArgumentException("getLifecycleManager is not available.");
        //noinspection unchecked
        return (LifecycleEventManager<Plugin>) GET_LIFECYCLE_MANAGER.invoke(plugin);
    }

    public static void ensureSetup() {

    }
}
