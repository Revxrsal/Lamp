package revxrsal.commands.jda.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.CommandPath;
import revxrsal.commands.jda.JDACommandHandler;
import revxrsal.commands.jda.core.adapter.SlashCommandAdapter;

/**
 * A utility class for parsing Lamp's components into JDA's. And JDA component into raw strings.
 */
public final class SlashCommandConverter {
    private SlashCommandConverter() {
    }

    /**
     * Parses all the registered commands and categories in the given {@link JDACommandHandler}
     * and returns converted CommandData collection
     *
     * @param commandHandler The {@link JDACommandHandler} instance used to get all commands and categories
     * @return Converted CommandData collection
     */
    public static Collection<? extends CommandData> convertCommands(JDACommandHandler commandHandler) {
        validateCommandPaths(commandHandler);
        List<SlashCommandData> commandDataList = new ArrayList<>();
        List<CommandCategory> roots = commandHandler.getCategories()
                .values()
                .stream()
                .filter(category -> category.getParent() == null)
                .collect(Collectors.toList());
        List<ExecutableCommand> rootCommands = commandHandler.getCommands()
                .values()
                .stream()
                .filter(command -> command.getPath().isRoot())
                .collect(Collectors.toList());
        for (CommandCategory root : roots) {
            String rootCommandPath = root.getPath().getFirst();
            commandDataList.add(parseCategory(commandHandler, root, Commands.slash(rootCommandPath, rootCommandPath)));
        }
        for (ExecutableCommand root : rootCommands)
            commandDataList.add(parseCommand(commandHandler, root));
        return commandDataList;
    }

    private static SlashCommandData parseCommand(JDACommandHandler commandHandler, ExecutableCommand command) {
        if (command.getPath().size() > 3)
            throw new IllegalArgumentException("Command path for JDA slash commands cannot be longer than 3. Path '" + command.getPath().toRealString() + "'");

        String rootCommandPath = command.getPath().getFirst();
        String commandDescription = Optional.ofNullable(command.getDescription()).orElse(rootCommandPath);
        if (!command.getPath().isRoot())
            return null;
        SlashCommandData commandData = Commands.slash(rootCommandPath, commandDescription);
        commandHandler.getSlashCommandMappers().forEach(mapper -> mapper.mapSlashCommand(SlashCommandAdapter.of(commandData), command));
        return commandData;
    }

    private static void parseSubcommand(JDACommandHandler commandHandler, ExecutableCommand command, SlashCommandData commandData) {
        if (command.getPath().size() > 3)
            throw new IllegalArgumentException("Command path for JDA subcommands cannot be longer than 3. Path '" + command.getPath().toRealString() + "'");
        String subcommandPath = command.getName();
        String commandDescription = Optional.ofNullable(command.getDescription()).orElse(subcommandPath);
        SubcommandData subcommandData = new SubcommandData(subcommandPath, commandDescription);
        commandHandler.getSlashCommandMappers().forEach(mapper -> mapper.mapSlashCommand(SlashCommandAdapter.of(subcommandData), command));
        if (command.getPath().size() == 2) {
            commandData.addSubcommands(subcommandData);
            return;
        }

        String subcommandGroupPath = command.getParent().getName();
        SubcommandGroupData subcommandGroup = commandData.getSubcommandGroups()
                .stream()
                .filter(group -> group.getName().equals(subcommandGroupPath))
                .findFirst()
                .orElseGet(() -> {
                    SubcommandGroupData group = new SubcommandGroupData(subcommandGroupPath, subcommandGroupPath);
                    commandData.addSubcommandGroups(group);
                    return group;
                });
        subcommandGroup.addSubcommands(subcommandData);
    }

    private static SlashCommandData parseCategory(JDACommandHandler commandHandler, CommandCategory category, SlashCommandData parentCommand) {
        if (category.getDefaultAction() != null)
            parseSubcommand(commandHandler, category.getDefaultAction(), parentCommand);

        for (CommandCategory children : category.getCategories().values())
            parseCategory(commandHandler, children, parentCommand);

        for (ExecutableCommand command : category.getCommands().values())
            parseSubcommand(commandHandler, command, parentCommand);
        return parentCommand;
    }

    private static void validateCommandPaths(JDACommandHandler commandHandler) {
        List<CommandPath> commandPaths = Stream.of(commandHandler.getCommands().keySet(),
                        commandHandler.getCategories()
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getValue().getParent() == null && entry.getValue().getDefaultAction() != null)
                                .map(Entry::getKey)
                                .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        for (CommandPath path : commandPaths) {
            Optional<CommandPath> conflictingPath = commandPaths.stream().filter(childPath -> !childPath.equals(path) && path.isChildOf(childPath)).findFirst();
            if (!conflictingPath.isPresent())
                continue;
            throw new IllegalArgumentException(
                    "Paths `" + path.toRealString() + "` and `" + conflictingPath.get().toRealString() + "` are conflicting, remove or modify one of paths");
        }
    }
}
