package revxrsal.commands.autocomplete;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.*;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutocompleteResolutionTest {

    enum Mode {FAST, SLOW}

    static final class Position {}

    /**
     * Reads two numbers, like a 2D coordinate
     */
    static final class PositionType implements ParameterType<CommandActor, Position> {
        @Override public Position parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<CommandActor> context) {
            input.readInt();
            input.skipWhitespace();
            input.readInt();
            return new Position();
        }

        @Override public @NotNull SuggestionProvider<CommandActor> defaultSuggestions() {
            return context -> Arrays.asList("0 0", "10 10");
        }
    }

    private Lamp<CommandActor> lamp;
    private CommandActor actor;

    @BeforeEach
    void setUp() {
        LookupType.parses = 0;
        lamp = Lamp.builder()
                .parameterTypes(types -> types
                        .addParameterType(Position.class, new PositionType())
                        .addParameterType(Lookup.class, new LookupType()))
                .build();
        actor = new AutocompleteFlagsTest.TestActor(lamp);
        lamp.register(new Commands());
    }

    private void assertSuggestions(String input, String... expected) {
        List<String> suggestions = lamp.autoCompleter().complete(actor, input);
        assertEquals(new HashSet<>(Arrays.asList(expected)), new HashSet<>(suggestions), "for input '" + input + "'");
    }

    @Test
    void completesRootCommands() {
        assertSuggestions("isl", "island");
        assertSuggestions("ISL", "island");
    }

    @Test
    void completesSubcommands() {
        assertSuggestions("island ", "home", "help", "<name>");
        assertSuggestions("island h", "home", "help");
        assertSuggestions("island HO", "home");
    }

    @Test
    void doesNotSuggestHiddenCommands() {
        assertSuggestions("island a");
    }

    @Test
    void completesArgumentsBeforeLiterals() {
        assertSuggestions("team r", "red");
        assertSuggestions("team red ", "invite", "kick");
        assertSuggestions("team red k", "kick");
    }

    @Test
    void doesNotSuggestForArgumentsTheUserMovedPast() {
        assertSuggestions("give f", "fast");
        assertSuggestions("give f 5");
    }

    @Test
    void suggestsMultiWordArgumentsWhileTyping() {
        assertSuggestions("pos 1", "10 10");
        assertSuggestions("pos 10 1", "10");
    }

    @Test
    void doesNotParseTheArgumentBeingTyped() {
        assertSuggestions("lookup ab", "abc");
        assertEquals(0, LookupType.parses);
        assertSuggestions("lookup abc ", "x");
        assertEquals(1, LookupType.parses);
    }

    @Test
    void completesFlagsAfterNegativeNumbers() {
        assertSuggestions("move -1 --f", "--fast");
    }

    static final class Lookup {}

    /**
     * Stands in for a type whose parsing is expensive, like looking up a player by name
     */
    static final class LookupType implements ParameterType<CommandActor, Lookup> {
        static int parses = 0;

        @Override public Lookup parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<CommandActor> context) {
            parses++;
            input.readString();
            return new Lookup();
        }

        @Override public @NotNull SuggestionProvider<CommandActor> defaultSuggestions() {
            return context -> Arrays.asList("abc", "xyz");
        }
    }

    static class Commands {
        @Command("lookup") void lookup(Lookup lookup, @Suggest("x") String then) {}

        @Command("move") void move(int amount, @Switch("fast") boolean fast) {}

        @Command("island home") void home() {}

        @Command("island help") void help() {}

        @Command("island <name>") void named(@Named("name") @Suggest("<name>") String name) {}

        @Command("island admin") @SecretCommand void admin() {}

        @Command("team <team> invite") void invite(@Named("team") @Suggest({"red", "blue"}) String team) {}

        @Command("team <team> kick") void kick(@Named("team") @Suggest({"red", "blue"}) String team) {}

        @Command("give") void give(Mode mode, int amount) {}

        @Command("pos") void pos(Position position) {}
    }
}
