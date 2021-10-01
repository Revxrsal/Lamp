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

    public Map<CommandPath, CommandExecutable> getExecutables() {
        return executables;
    }

    public Map<CommandPath, BaseCommandCategory> getSubcategories() {
        return subcategories;
    }
}
