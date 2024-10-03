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
package revxrsal.commands.util;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.addAll;
import static revxrsal.commands.util.Preconditions.cannotInstantiate;
import static revxrsal.commands.util.Reflections.getTopClasses;

public final class CommandPaths {

    private CommandPaths() {
        cannotInstantiate(CommandPaths.class);
    }

    public static @NotNull List<String> parseCommandAnnotations(@NotNull Class<?> container, @NotNull CommandFunction function) {
        List<String> commands = new ArrayList<>();
        List<String> subcommands = new ArrayList<>();

        Command commandAnnotation = function.annotations().require(
                Command.class,
                "Method " + function.name() + " does not have a parent command! You might have forgotten one of the following:\n" +
                        "- @Command on the method or class\n" +
                        "- implement OrphanCommand");
        Preconditions.notEmpty(commandAnnotation.value(), "@Command#value() cannot be an empty array!");
        addAll(commands, commandAnnotation.value());

        List<String> parentSubcommandAliases = new ArrayList<>();

        for (Class<?> topClass : getTopClasses(container)) {
            AnnotationList annotations = AnnotationList.create(topClass)
                    .replaceAnnotations(topClass, function.lamp().annotationReplacers());
            Subcommand ps = annotations.get(Subcommand.class);
            if (ps != null) {
                addAll(parentSubcommandAliases, ps.value());
            }
        }

        Subcommand subcommandAnnotation = function.annotations().get(Subcommand.class);
        if (subcommandAnnotation != null) {
            if (parentSubcommandAliases.isEmpty()) {
                addAll(subcommands, subcommandAnnotation.value());
            } else {
                for (String parentSubcommandAlias : parentSubcommandAliases) {
                    Arrays.stream(subcommandAnnotation.value()).map(v -> parentSubcommandAlias + ' ' + v)
                            .forEach(subcommands::add);
                }
            }
        }

        List<String> paths = new ArrayList<>();
        for (String command : commands) {
            if (!subcommands.isEmpty()) {
                for (String subcommand : subcommands) {
                    paths.add(command + ' ' + subcommand);
                }
            } else {
                paths.add(command);
            }
        }
        return paths;
    }
}
