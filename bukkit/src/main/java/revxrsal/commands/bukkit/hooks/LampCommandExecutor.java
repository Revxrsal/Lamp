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
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.util.BukkitUtils;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.List;
import java.util.StringJoiner;

import static revxrsal.commands.util.Collections.map;
import static revxrsal.commands.util.Strings.stripNamespace;

public final class LampCommandExecutor<A extends BukkitCommandActor> implements TabExecutor {

    private final @NotNull Lamp<A> lamp;
    private final @NotNull ActorFactory<A> senderToActor;

    public LampCommandExecutor(@NotNull Lamp<A> lamp, @NotNull ActorFactory<A> senderToActor) {
        this.lamp = lamp;
        this.senderToActor = senderToActor;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        A actor = senderToActor.create(sender, lamp);

        MutableStringStream input = createInput(command.getName(), args);
        lamp.dispatch(actor, input);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        A actor = senderToActor.create(sender, lamp);
        MutableStringStream input = createInput(command.getName(), args);
        List<String> completions = lamp.autoCompleter().complete(actor, input);
        if (BukkitUtils.isBrigadierAvailable()) {
            return completions; // brigadier allows suggestions with spaces
        } else {
            // on older versions, we get funny behavior when suggestions contain spaces
            return map(completions, LampCommandExecutor::ignoreAfterSpace);
        }
    }

    private static String ignoreAfterSpace(String v) {
        int spaceIndex = v.indexOf(' ');
        return spaceIndex == -1 ? v : v.substring(0, spaceIndex);
    }

    private static @NotNull MutableStringStream createInput(String commandName, String[] args) {
        StringJoiner userInput = new StringJoiner(" ");
        userInput.add(stripNamespace(commandName));
        for (@NotNull String arg : args)
            userInput.add(arg);
        return StringStream.createMutable(userInput.toString());
    }
}
