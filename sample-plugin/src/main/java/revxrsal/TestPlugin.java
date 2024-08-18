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
package revxrsal;

import com.mojang.brigadier.arguments.ArgumentType;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.BukkitArgumentTypes;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.EntitySelector;
import revxrsal.commands.bukkit.brigadier.MinecraftArgumentType;

import static io.papermc.paper.command.brigadier.argument.ArgumentTypes.blockState;

public class TestPlugin extends JavaPlugin {

    @Override public void onEnable() {
        ArgumentTypes<BukkitCommandActor> types = BukkitArgumentTypes.builder(BukkitCommandActor.class)
                .addType(BlockState.class, blockState())
                .build();
        Lamp<BukkitCommandActor> lamp = BukkitLamp
                .defaultBuilder(this, types)
                .build();
        lamp.register(this);
        ArgumentType<Object> x = MinecraftArgumentType.COLOR.get();
        System.out.println(x);
    }

    @Command("test <players> show")
    public void test(
            Player player,
            EntitySelector<Player> players
    ) {
        player.sendMessage("Wow! It's working: " + player);
    }
}
