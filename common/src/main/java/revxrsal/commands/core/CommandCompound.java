package revxrsal.commands.core;

import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@ToString
public final class CommandCompound {

    private final Map<CommandPath, CommandExecutable> executables;
    private final Map<CommandPath, BaseCommandCategory> subcategories;

    public CommandCompound() {
        executables = new HashMap<>();
        subcategories = new HashMap<>();
    }

    public CommandExecutable getExec(@NotNull CommandPath path) {
        return executables.get(path);
    }

    public BaseCommandCategory getCat(@NotNull CommandPath path) {
        return subcategories.get(path);
    }

    public void addAll(@NotNull CommandCompound other) {
        other.executables.keySet().forEach(path -> {
            if (executables.containsKey(path))
                throw new IllegalStateException("A command with path '" + path.toRealString() + "' already exists!");
        });

        executables.putAll(other.executables);
        subcategories.putAll(other.subcategories);

        // try once again to assign children to parents. since methods in classes
        // are not sorted by any order a child may be registered before a parent is.
        // now it is guaranteed that everyone is registered, so we try again.
        subcategories.forEach((path, category) -> {
            if (category.parent == null) {
                category.parent(subcategories.get(path.getCategoryPath()));
            }
        });
        executables.forEach((path, command) -> {
            if (command.parent == null) {
                command.parent(subcategories.get(path.getCategoryPath()));
            }
        });
    }

    public Map<CommandPath, CommandExecutable> getExecutables() {
        return executables;
    }

    public Map<CommandPath, BaseCommandCategory> getSubcategories() {
        return subcategories;
    }
}
