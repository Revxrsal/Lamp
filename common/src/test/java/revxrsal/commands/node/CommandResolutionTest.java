package revxrsal.commands.node;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.*;
import revxrsal.commands.command.CommandActor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommandResolutionTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface AdminOnly {}

    private final List<String> ran = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final List<String> replies = new ArrayList<>();
    private Lamp<CommandActor> lamp;
    private CommandActor actor;

    @BeforeEach
    void setUp() {
        setUp(false);
    }

    private void setUp(boolean rejectTrailingInput) {
        ran.clear();
        lamp = Lamp.builder()
                .permissionFactory((annotations, lamp) -> annotations.contains(AdminOnly.class) ? actor -> false : null)
                .dispatcherSettings(DispatcherSettings.builder().rejectTrailingInput(rejectTrailingInput).build())
                .build();
        actor = new TestActor();
    }

    private void dispatch(String input) {
        ran.clear();
        errors.clear();
        replies.clear();
        lamp.dispatch(actor, input);
    }

    private void assertRan(String... expected) {
        assertEquals(Arrays.asList(expected), ran);
    }

    @Test
    void firstDifferenceDecidesSpecificity() {
        lamp.register(new Object() {
            @Command("foo <a> bar") void a(@Named("a") String a) {ran.add("foo <a> bar");}

            @Command("foo baz <b>") void b(@Named("b") String b) {ran.add("foo baz <b>");}
        });
        dispatch("foo baz bar");
        assertRan("foo baz <b>");
    }

    @Test
    void argumentsBetweenLiterals() {
        lamp.register(new Object() {
            @Command("team <team> invite <player>") void invite(@Named("team") String team, @Named("player") String player) {ran.add("invite " + team + " " + player);}

            @Command("team <team> kick <player>") void kick(@Named("team") String team, @Named("player") String player) {ran.add("kick " + team + " " + player);}

            @Command("team create <name>") void create(@Named("name") String name) {ran.add("create " + name);}
        });
        dispatch("team red kick bob");
        assertRan("kick red bob");
        dispatch("team create blue");
        assertRan("create blue");
        // Both match, as the last String is greedy. The literal 'create' wins over
        // <team>, as that is the first position at which the two commands differ.
        dispatch("team create invite bob");
        assertRan("create invite bob");
    }

    @Test
    void fullMatchIsFoundBehindManyPartialMatches() {
        lamp.register(new Object() {
            @Command("t") void t0() {ran.add("t");}

            @Command("t") void t1(int x) {ran.add("int");}

            @Command("t") void t2(long x) {ran.add("long");}

            @Command("t") void t3(double x) {ran.add("double");}

            @Command("t") void t4(float x) {ran.add("float");}

            @Command("t") void t5(int a, int b, int c) {ran.add("int int int");}
        });
        dispatch("t 1 2 3");
        assertRan("int int int");
    }

    @Test
    void catchAllIsReachedBehindManyFailures() {
        lamp.register(new Object() {
            @Command("c") void c1(int x) {ran.add("int");}

            @Command("c") void c2(long x) {ran.add("long");}

            @Command("c") void c3(double x) {ran.add("double");}

            @Command("c") void c4(float x) {ran.add("float");}

            @Command("c") void c5(boolean x) {ran.add("boolean");}

            @Command("c") void c6(String x) {ran.add("string");}
        });
        dispatch("c abc");
        assertRan("string");
    }

    @Test
    void fullMatchBeatsShorterCommand() {
        lamp.register(new Object() {
            @Command("ping") void ping() {ran.add("ping");}

            @Command("ping help") void help() {ran.add("ping help");}
        });
        dispatch("ping help");
        assertRan("ping help");
        dispatch("ping");
        assertRan("ping");
    }

    enum Mode {FAST, SLOW}

    @Test
    void typePriorityDecidesBetweenOverlappingTypes() {
        lamp.register(new Object() {
            @Command("mode") void string(String s) {ran.add("string " + s);}

            @Command("mode") void mode(Mode mode) {ran.add("mode " + mode);}
        });
        dispatch("mode fast");
        assertRan("mode FAST");
        dispatch("mode other");
        assertRan("string other");
    }

    @Test
    void missingPermissionFallsThroughToAnotherOverload() {
        lamp.register(new Object() {
            @Command("perm") @AdminOnly void admin(int x) {ran.add("admin");}

            @Command("perm") void anyone(String s) {ran.add("anyone");}
        });
        dispatch("perm 5");
        assertRan("anyone");
    }

    @Test
    void cooldownDoesNotFallThroughToAnotherOverload() {
        lamp.register(new Object() {
            @Command("cd") @Cooldown(10) void limited(int x) {ran.add("limited");}

            @Command("cd") void other(String s) {ran.add("other");}
        });
        dispatch("cd 5");
        assertRan("limited");
        dispatch("cd 5");
        assertRan();
        assertEquals(1, errors.size());
    }

    @Test
    void suggestsClosestSubcommands() {
        lamp.register(new Islands());
        dispatch("island hoem");
        assertRan();
        assertEquals("- island home", replies.get(0));
        assertFalse(replies.contains("- island delete"));
    }

    @Test
    void doesNotSuggestHiddenSubcommands() {
        lamp.register(new Islands());
        dispatch("island admn");
        assertFalse(replies.contains("- island admin"));
        assertFalse(replies.contains("- island adminpanel"));
    }

    @Test
    void suggestsClosestRootCommand() {
        lamp.register(new Islands());
        dispatch("islnd home");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Did you mean: island"), errors.get(0));
    }

    @Test
    void trailingInputRunsByDefault() {
        lamp.register(new Object() {
            @Command("solo") void solo() {ran.add("solo");}
        });
        dispatch("solo extra");
        assertRan("solo");
    }

    @Test
    void trailingInputCanBeRejected() {
        setUp(true);
        lamp.register(new Object() {
            @Command("solo") void solo() {ran.add("solo");}
        });
        dispatch("solo extra");
        assertRan();
        assertTrue(errors.get(0).contains("Too many arguments"), errors.get(0));
        dispatch("solo ");
        assertRan("solo");
    }

    @Test
    void flagsMayAppearBetweenLiterals() {
        lamp.register(new Object() {
            @Command("fl sub") void sub(@Switch("x") boolean x) {ran.add("x=" + x);}
        });
        dispatch("fl -x sub");
        assertRan("x=true");
    }

    @Test
    void unregisteredCommandsAreNotDispatched() {
        lamp.register(new Islands());
        lamp.unregisterIf(command -> command.path().equals("island home"));
        dispatch("island home");
        assertRan();
        dispatch("island help");
        assertRan("help");
    }

    @Test
    void tabAfterLiteralDoesNotThrow() {
        lamp.register(new Islands());
        assertDoesNotThrow(() -> dispatch("island\thome"));
    }

    @Command("island")
    static class Islands {
        @Subcommand("home") void home() {ran("home");}

        @Subcommand("help") void help() {ran("help");}

        @Subcommand("create") void create() {ran("create");}

        @Subcommand("delete") void delete() {ran("delete");}

        @Subcommand("admin") @SecretCommand void admin() {ran("admin");}

        @Subcommand("adminpanel") @AdminOnly void adminPanel() {ran("adminpanel");}

        private void ran(String name) {
            CommandResolutionTest.current.ran.add(name);
        }
    }

    // Lets the static Islands class record into the running test
    private static CommandResolutionTest current;

    {
        current = this;
    }

    private final class TestActor implements CommandActor {

        @NotNull @Override public String name() {
            return "test";
        }

        @NotNull @Override public UUID uniqueId() {
            return new UUID(0, 0);
        }

        @Override public void sendRawMessage(@NotNull String message) {
            replies.add(message);
        }

        @Override public void sendRawError(@NotNull String message) {
            errors.add(message);
        }

        @NotNull @Override public Lamp<?> lamp() {
            return lamp;
        }
    }
}
