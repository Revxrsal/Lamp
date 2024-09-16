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
package revxrsal.commands.fabric.hooks;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.brigadier.BrigadierConverter;
import revxrsal.commands.brigadier.BrigadierParser;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.fabric.FabricLampConfig;
import revxrsal.commands.fabric.actor.FabricCommandActor;
import revxrsal.commands.hook.CancelHandle;
import revxrsal.commands.hook.CommandRegisteredHook;
import revxrsal.commands.node.ParameterNode;

import static net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT;

/**
 * A hook that registers Lamp commands into Fabric
 *
 * @param <A> The actor type
 */
public final class FabricCommandHooks<A extends FabricCommandActor> implements CommandRegisteredHook<A>, BrigadierConverter<A, ServerCommandSource> {

    private final FabricLampConfig<A> config;
    private final RootCommandNode<ServerCommandSource> root = new RootCommandNode<>();
    private final BrigadierParser<ServerCommandSource, A> parser = new BrigadierParser<>(this);

    public FabricCommandHooks(FabricLampConfig<A> config) {
        this.config = config;
        EVENT.register((dispatcher, registryAccess, environment) -> {
            for (CommandNode<ServerCommandSource> child : root.getChildren()) {
                dispatcher.getRoot().addChild(child);
            }
        });
    }

    @Override
    public void onRegistered(@NotNull ExecutableCommand<A> command, @NotNull CancelHandle cancelHandle) {
        LiteralCommandNode<ServerCommandSource> node = parser.createNode(command);
        root.addChild(node);
    }

    @Override
    public @NotNull ArgumentType<?> getArgumentType(@NotNull ParameterNode<A, ?> parameter) {
        return config.argumentTypes().type(parameter);
    }

    @Override
    public @NotNull A createActor(@NotNull ServerCommandSource source, @NotNull Lamp<A> lamp) {
        return config.actorFactory().create(source, lamp);
    }
}
