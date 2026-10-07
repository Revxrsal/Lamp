package revxrsal.commands.autocomplete;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Flag;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.command.CommandActor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutocompleteFlagsTest {

    private Lamp<CommandActor> lamp;
    private CommandActor actor;

    @BeforeEach
    void setUp() {
        lamp = Lamp.builder().build();
        actor = new TestActor(lamp);
        lamp.register(new FlagCommands());
    }

    @Test
    @Timeout(2)
    void bareWordsAfterFlagsDoNotHangAutocomplete() {
        assertSuggestions(lamp.autoCompleter().complete(actor, "alts Notch abc"), "--chain", "--since");
    }

    @Test
    @Timeout(2)
    void bareWordsAfterLongSwitchesDoNotHangAutocomplete() {
        assertSuggestions(lamp.autoCompleter().complete(actor, "alts Notch --chain abc"), "--since");
    }

    @Test
    @Timeout(2)
    void flagsAfterLongSwitchesDoNotReuseSwitchParserState() {
        assertSuggestions(lamp.autoCompleter().complete(actor, "alts Notch --chain abc --since yesterday"));
    }

    @Test
    @Timeout(2)
    void trailingWhitespaceAfterBareWordsDoesNotHangAutocomplete() {
        assertSuggestions(lamp.autoCompleter().complete(actor, "alts Notch abc "), "--chain", "--since");
    }

    @Test
    @Timeout(2)
    void nonSpaceWhitespaceAfterFlagsDoesNotHangAutocomplete() {
        assertSuggestions(lamp.autoCompleter().complete(actor, "alts Notch\tabc"), "--chain", "--since");
    }

    private static void assertSuggestions(List<String> suggestions, String... expected) {
        assertEquals(new HashSet<>(Arrays.asList(expected)), new HashSet<>(suggestions));
    }

    static class FlagCommands {

        @Command("alts")
        public void alts(String query, @Switch("chain") boolean chain, @Optional @Flag("since") String since) {
        }
    }

    static class TestActor implements CommandActor {

        private final Lamp<?> lamp;

        TestActor(Lamp<?> lamp) {
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
