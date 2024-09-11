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

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.brigadier.BrigadierAdapter;
import revxrsal.commands.brigadier.BrigadierConverter;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.ParameterNode;

import java.lang.reflect.Method;

final class ByPaperLifecycle<A extends BukkitCommandActor> implements BukkitBrigadierBridge<A>, BrigadierConverter<A, CommandSourceStack> {

    private final ArgumentTypes<A> types;
    private final ActorFactory<A> actorFactory;

    private final RootCommandNode<CommandSourceStack> root = new RootCommandNode<>();

    public ByPaperLifecycle(JavaPlugin plugin, ArgumentTypes<A> types, ActorFactory<A> actorFactory) {
        this.types = types;
        this.actorFactory = actorFactory;
        getLifecycleManager(plugin).registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            for (CommandNode<CommandSourceStack> node : root.getChildren()) {
                event.registrar().register(((LiteralCommandNode<CommandSourceStack>) node));
            }
        });
    }

    @Override public void register(ExecutableCommand<A> command) {
        LiteralCommandNode<CommandSourceStack> node = BrigadierAdapter.createNode(command, this);
        root.addChild(node);
    }

    @Override public @NotNull ArgumentType<?> getArgumentType(@NotNull ParameterNode<A, ?> parameter) {
        return types.type(parameter);
    }

    @Override public @NotNull A createActor(@NotNull CommandSourceStack sender, @NotNull Lamp<A> lamp) {
        return actorFactory.create(sender.getExecutor() == null ? sender.getSender() : sender.getExecutor(), lamp);
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

    @SneakyThrows private static LifecycleEventManager<Plugin> getLifecycleManager(JavaPlugin plugin) {
        if (GET_LIFECYCLE_MANAGER == null)
            throw new IllegalArgumentException("getLifecycleManager is not available.");
        //noinspection unchecked
        return (LifecycleEventManager<Plugin>) GET_LIFECYCLE_MANAGER.invoke(plugin);
    }

}
