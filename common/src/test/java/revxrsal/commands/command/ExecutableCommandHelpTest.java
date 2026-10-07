package revxrsal.commands.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.help.Help;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

class ExecutableCommandHelpTest {

    private Lamp<CommandActor> lamp;

    @BeforeEach
    void setUp() {
        lamp = Lamp.builder().build();
    }

    @Test
    void testSiblingCommands() {
        lamp.register(new CommandA(), new CommandB());
        ExecutableCommand<CommandActor> commandA = getCommand("a");
        ExecutableCommand<CommandActor> commandAA = getCommand("a a");
        ExecutableCommand<CommandActor> commandB = getCommand("b");

        assertPathsEquals(commandAA.siblingCommands(), "a b", "a");
        assertPathsEquals(commandA.siblingCommands(), "a a", "a b");
        assertPathsEquals(commandB.siblingCommands());
    }

    @Test
    void testChildrenCommands() {
        lamp.register(new CommandA(), new CommandB());
        ExecutableCommand<CommandActor> commandA = getCommand("a");
        ExecutableCommand<CommandActor> commandAA = getCommand("a a");
        ExecutableCommand<CommandActor> commandB = getCommand("b");

        assertPathsEquals(commandAA.childrenCommands());
        assertPathsEquals(commandA.childrenCommands(), "a a", "a b");
        assertPathsEquals(commandB.childrenCommands());
    }

    @Test
    void testRelatedCommands() {
        lamp.register(new CommandA(), new CommandB());
        ExecutableCommand<CommandActor> commandA = getCommand("a");
        ExecutableCommand<CommandActor> commandAA = getCommand("a a");
        ExecutableCommand<CommandActor> commandB = getCommand("b");

        assertPathsEquals(commandAA.relatedCommands(), "a b", "a");
        assertPathsEquals(commandA.relatedCommands(), "a a", "a b");
        assertPathsEquals(commandB.relatedCommands());
    }

    @Test
    void testRelatedCommandsIncludeNestedSubcommands() {
        lamp.register(new NestedCommands(), new PrefixedCommands());
        ExecutableCommand<CommandActor> rootHelp = getCommand("n help [page]");
        ExecutableCommand<CommandActor> arenaHelp = getCommand("n arena help [page]");

        assertPathsEquals(rootHelp.relatedCommands(),
                "n reload", "n arena enable <arena>", "n arena parkour test <arena>",
                "n arena help [page]");
        assertPathsEquals(arenaHelp.relatedCommands(),
                "n arena enable <arena>", "n arena parkour test <arena>");

        // siblings and children keep their strict meaning
        assertPathsEquals(rootHelp.siblingCommands(), "n reload");
        assertPathsEquals(rootHelp.childrenCommands());
    }

    @Test
    void testRelatedCommandsIncludeNestedSubcommandsOfRoot() {
        lamp.register(new CommandA(), new NestedA());
        ExecutableCommand<CommandActor> commandA = getCommand("a");
        ExecutableCommand<CommandActor> commandAA = getCommand("a a");

        assertPathsEquals(commandA.relatedCommands(), "a a", "a b", "a c d");
        assertPathsEquals(commandAA.relatedCommands(), "a b", "a", "a c d");
    }

    private ExecutableCommand<CommandActor> getCommand(String path) {
        return lamp.registry().commands().stream().filter(cmd -> cmd.path().equals(path))
                .findFirst().get();
    }

    private static void assertPathsEquals(Help.CommandList<CommandActor> commands,
            String... paths) {
        // we use sets here because we don't care about ordering
        assertEquals(new HashSet<>(Arrays.asList(paths)),
                commands.all().stream().map(cmd -> cmd.path()).collect(Collectors.toSet()));
    }

    @Command("a")
    class CommandA {

        @CommandPlaceholder
        public void placeholder() {}

        @Subcommand("a")
        public void a() {}

        @Subcommand("b")
        public void b() {}

    }

    @Command("b")
    class CommandB {

        @CommandPlaceholder
        public void b() {}

    }

    @Command("a c")
    class NestedA {

        @Subcommand("d")
        public void d() {}

    }

    @Command("n")
    class NestedCommands {

        @Subcommand("help")
        public void help(@Default("1") @Named("page") int page) {}

        @Subcommand("reload")
        public void reload() {}

        @Subcommand("arena help")
        public void arenaHelp(@Default("1") @Named("page") int page) {}

        @Subcommand("arena enable")
        public void enable(@Named("arena") String arena) {}

        @Subcommand("arena parkour test")
        public void parkourTest(@Named("arena") String arena) {}

    }

    // shares a prefix with "n" but must never be related to it
    @Command("nn")
    class PrefixedCommands {

        @Subcommand("arena enable")
        public void enable(@Named("arena") String arena) {}

    }

}
