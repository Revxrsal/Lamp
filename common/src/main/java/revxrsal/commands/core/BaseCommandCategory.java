/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
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
package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class BaseCommandCategory implements CommandCategory {

    // lazily populated by CommandParser
    CommandPath path;
    String name;
    @Nullable BaseCommandCategory parent;
    @Nullable CommandExecutable defaultAction;
    CommandHandler handler;

    final Map<CommandPath, ExecutableCommand> commands = new HashMap<>();
    final Map<CommandPath, BaseCommandCategory> categories = new HashMap<>();
    final CommandPermission permission = new CategoryPermission();

    @Override public @NotNull String getName() {
        return name;
    }

    @Override public @NotNull CommandPath getPath() {
        return path;
    }

    @Override public @NotNull CommandHandler getCommandHandler() {
        return handler;
    }

    @Override public @Nullable CommandCategory getParent() {
        return parent;
    }

    @Override public @Nullable ExecutableCommand getDefaultAction() {
        return defaultAction;
    }

    @Override public @NotNull CommandPermission getPermission() {
        return permission;
    }

    @Override public boolean isSecret() {
        for (ExecutableCommand command : commands.values()) {
            if (command.isSecret()) continue;
            return false;
        }
        for (CommandCategory category : categories.values()) {
            if (category.isSecret()) continue;
            return false;
        }
        return true;
    }

    @Override public boolean isEmpty() {
        return defaultAction == null && commands.isEmpty() && categories.isEmpty();
    }

    private final Map<CommandPath, CommandCategory> unmodifiableCategories = Collections.unmodifiableMap(categories);

    @Override public @NotNull @UnmodifiableView Map<CommandPath, CommandCategory> getCategories() {
        return unmodifiableCategories;
    }

    private final Map<CommandPath, ExecutableCommand> unmodifiableCommands = Collections.unmodifiableMap(commands);

    @Override public @NotNull @UnmodifiableView Map<CommandPath, ExecutableCommand> getCommands() {
        return unmodifiableCommands;
    }

    @Override public String toString() {
        return "CommandCategory{path=" + path + ", name='" + name + "'}";
    }

    public void parent(BaseCommandCategory cat) {
        parent = cat;
        if (cat != null)
            cat.categories.put(path, this);
    }

    /**
     * Category permission: They have access to the category if they have
     * access to any of its commands or other categories.
     */
    private class CategoryPermission implements CommandPermission {

        @Override public boolean canExecute(@NotNull CommandActor actor) {
            for (ExecutableCommand command : commands.values())
                if (command.getPermission().canExecute(actor))
                    return true;
            for (CommandCategory category : categories.values())
                if (category.getPermission().canExecute(actor))
                    return true;
            if (defaultAction == null)
                return false;
            return defaultAction.hasPermission(actor);
        }
    }

    @Override public int compareTo(@NotNull CommandCategory o) {
        return path.compareTo(o.getPath());
    }
}
