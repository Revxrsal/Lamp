package revxrsal.commands.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.command.CommandCategory;
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

    final Map<CommandPath, ExecutableCommand> commands = new HashMap<>();
    final Map<CommandPath, BaseCommandCategory> categories = new HashMap<>();

    @Override public @NotNull String getName() {
        return name;
    }

    @Override public @NotNull CommandPath getPath() {
        return path;
    }

    @Override public @Nullable CommandCategory getParent() {
        return parent;
    }

    @Override public @Nullable ExecutableCommand getDefaultAction() {
        return defaultAction;
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
}
