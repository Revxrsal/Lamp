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
package revxrsal.commands.paper.brigadier.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.brigadier.BrigadierAdapter;
import revxrsal.commands.brigadier.BrigadierParser;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.brigadier.BukkitBrigadierBridge;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.ParameterNode;

public final class ByPaperLifecycle<A extends BukkitCommandActor> implements BukkitBrigadierBridge<A>, BrigadierAdapter<A, CommandSourceStack> {

    private final JavaPlugin plugin;
    private final ArgumentTypes<A> types;
    private final ActorFactory<A> actorFactory;

    public ByPaperLifecycle(JavaPlugin plugin, ArgumentTypes<A> types, ActorFactory<A> actorFactory) {
        this.plugin = plugin;
        this.types = types;
        this.actorFactory = actorFactory;
    }

    @Override public void register(ExecutableCommand<A> command) {
        LiteralCommandNode<CommandSourceStack> node = BrigadierParser.createNode(command, this);
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                event -> event.registrar().register(node, command.description())
        );
    }

    @Override public @NotNull ArgumentType<?> getArgumentType(@NotNull ParameterNode<A, ?> parameter) {
        return types.type(parameter);
    }

    @Override public @NotNull A createActor(@NotNull CommandSourceStack sender, @NotNull Lamp<A> lamp) {
        return actorFactory.create(sender.getExecutor() == null ? sender.getSender() : sender.getExecutor(), lamp);
    }
}
