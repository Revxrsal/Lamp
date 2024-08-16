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
package revxrsal.commands.bukkit.hooks;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.List;
import java.util.StringJoiner;

import static revxrsal.commands.util.Strings.stripNamespace;

public final class DefaultLampCommandExecutor implements TabExecutor {

    private final @NotNull Lamp<BukkitCommandActor> lamp;

    public DefaultLampCommandExecutor(@NotNull Lamp<BukkitCommandActor> lamp) {
        this.lamp = lamp;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        BukkitCommandActor actor = new BasicBukkitActor(sender, lamp);

        MutableStringStream input = createInput(command.getName(), args);
        lamp.dispatch(actor, input);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        BukkitCommandActor actor = new BasicBukkitActor(sender, lamp);
        MutableStringStream input = createInput(command.getName(), args);
        return lamp.autoCompleter().complete(actor, input);
    }

    private static @NotNull MutableStringStream createInput(String commandName, String[] args) {
        StringJoiner userInput = new StringJoiner(" ");
        userInput.add(stripNamespace(commandName));
        for (@NotNull String arg : args)
            userInput.add(arg);
        return StringStream.createMutable(userInput.toString());
    }


}
