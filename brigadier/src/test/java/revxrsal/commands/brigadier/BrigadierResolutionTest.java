package revxrsal.commands.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.SuggestWith;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.node.ParameterNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BrigadierResolutionTest {

    private static String ran;
    private static Object seenTeam;

    private Lamp<CommandActor> lamp;
    private BrigadierParser<Object, CommandActor> parser;
    private CommandDispatcher<Object> dispatcher;

    @BeforeEach
    void setUp() {
        ran = null;
        seenTeam = null;
        lamp = Lamp.builder().build();
        parser = new BrigadierParser<>(new BrigadierConverter<CommandActor, Object>() {
            @Override public ArgumentType<?> getArgumentType(ParameterNode<CommandActor, ?> parameter) {
                return parameter.type() == int.class ? IntegerArgumentType.integer() : StringArgumentType.word();
            }

            @Override public CommandActor createActor(Object sender, Lamp<CommandActor> lamp) {
                return new TestActor(lamp);
            }
        });
        dispatcher = new CommandDispatcher<>();
    }

    private void register(Object commands) {
        lamp.register(commands);
        for (ExecutableCommand<CommandActor> command : lamp.registry().commands())
            BrigadierParser.addChild(dispatcher.getRoot(), parser.createNode(command));
    }

    @Test
    void lampDecidesWhichOverloadRuns() throws Exception {
        register(new Object() {
            @Command("diff") void string(@Named("s") String s) {ran = "string";}

            @Command("diff") void integer(@Named("n") int n) {ran = "int";}
        });
        dispatcher.execute("diff 5", new Object());
        assertEquals("int", ran);
        dispatcher.execute("diff abc", new Object());
        assertEquals("string", ran);
    }

    @Test
    void sameArgumentNameWithDifferentTypesFails() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> register(new Object() {
            @Command("same") void integer(@Named("x") int x) {}

            @Command("same") void string(@Named("x") String x) {}
        }));
        assertTrue(e.getMessage().contains("'x'"), e.getMessage());
    }

    @Test
    void sameArgumentNameWithSameTypeMerges() throws Exception {
        register(new Object() {
            @Command("team <team> invite") void invite(@Named("team") String team) {ran = "invite " + team;}

            @Command("team <team> kick") void kick(@Named("team") String team) {ran = "kick " + team;}
        });
        dispatcher.execute("team red kick", new Object());
        assertEquals("kick red", ran);
    }

    @Test
    void suggestionsSeeEarlierArguments() throws Exception {
        register(new Members());
        dispatcher.getCompletionSuggestions(dispatcher.parse("team red kick f", new Object())).get();
        assertEquals("red", seenTeam);
        seenTeam = null;
        dispatcher.getCompletionSuggestions(dispatcher.parse("team red kick ", new Object())).get();
        assertEquals("red", seenTeam);
    }

    enum Member {FRANK, GEORGE}

    public static class MemberSuggestions implements SuggestionProvider<CommandActor> {
        @Override public Collection<String> getSuggestions(ExecutionContext<CommandActor> context) {
            seenTeam = context.getResolvedArgumentOrNull("team");
            return Arrays.asList("frank", "george");
        }
    }

    public static class Members {
        @Command("team <team> kick <member>")
        public void kick(@Named("team") String team, @Named("member") @SuggestWith(MemberSuggestions.class) Member member) {}
    }

    private static final class TestActor implements CommandActor {
        private final Lamp<?> lamp;

        private TestActor(Lamp<?> lamp) {
            this.lamp = lamp;
        }

        @Override public String name() {
            return "test";
        }

        @Override public UUID uniqueId() {
            return new UUID(0, 0);
        }

        @Override public void sendRawMessage(String message) {
        }

        @Override public void sendRawError(String message) {
        }

        @Override public Lamp<?> lamp() {
            return lamp;
        }
    }
}
