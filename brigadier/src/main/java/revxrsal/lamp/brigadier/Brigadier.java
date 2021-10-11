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
package revxrsal.lamp.brigadier;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.CommandPath;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

public final class Brigadier {

    private Brigadier() {}

    public static Collection<LiteralArgumentBuilder<?>> parse(@NotNull CommandHandler handler) {
        Map<CommandPath, LiteralArgumentBuilder<?>> nodes = new HashMap<>();
        for (CommandCategory value : handler.getCategories().values()) {
            nodes.put(value.getPath(), literal(value.getName()));
        }
        for (CommandCategory value : handler.getCategories().values()) {
            CommandCategory parent = value.getParent();
            if (parent != null) {
                nodes.get(parent.getPath()).then(literal(value.getName()));
            }
        }
        for (ExecutableCommand command : handler.getCommands().values()) {
            nodes.put(command.getPath(), literal(command.getName()));
            CommandCategory parent = command.getParent();
            if (parent != null) {
                nodes.get(parent.getPath()).then(literal(command.getName()));
            }
        }
        return nodes.values();
    }

    private void addParameter(CommandParameter parameter, LiteralArgumentBuilder<?> node) {
        if (parameter.getType() == String.class)
            node.then(argument("", StringArgumentType.word()));
    }

    private static final Brigadier BRIGADIER = new Brigadier();

    public static Brigadier brigadier() {
        return BRIGADIER;
    }
}
