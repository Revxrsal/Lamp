package revxrsal.commands.sponge.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.*;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.sponge.actor.SpongeCommandActor;

import static revxrsal.commands.sponge.util.SpongeUtils.legacyColorize;

public class SpongeExceptionHandler extends DefaultExceptionHandler<SpongeCommandActor> {

    @HandleException
    public void onInvalidPlayer(InvalidPlayerException e, SpongeCommandActor actor) {
        actor.error(legacyColorize("&cInvalid player: &e" + e.input() + "&c."));
    }

    @HandleException
    public void onInvalidPlayer(InvalidWorldException e, SpongeCommandActor actor) {
        actor.error(legacyColorize("&cInvalid world: &e" + e.input() + "&c."));
    }

    @HandleException
    public void onSenderNotConsole(SenderNotConsoleException e, SpongeCommandActor actor) {
        actor.error(legacyColorize("&cYou must be the console to execute this command!"));
    }

    @HandleException
    public void onSenderNotPlayer(SenderNotPlayerException e, SpongeCommandActor actor) {
        actor.error(legacyColorize("&cYou must be a player to execute this command!"));
    }

    @Override public void onEnumNotFound(@NotNull EnumNotFoundException e, @NotNull SpongeCommandActor actor) {
        actor.error(legacyColorize("&cInvalid choice: &e" + e.input() + "&c. Please enter a valid option from the available values."));
    }

    @Override public void onExpectedLiteral(@NotNull ExpectedLiteralException e, @NotNull SpongeCommandActor actor) {
        actor.error(legacyColorize("&cExpected &e" + e.node().name() + "&c, found &e" + e.input() + "&c."));
    }

    @Override public void onInputParse(@NotNull InputParseException e, @NotNull SpongeCommandActor actor) {
        switch (e.cause()) {
            case INVALID_ESCAPE_CHARACTER ->
                    actor.error(legacyColorize("&cInvalid input. Use &e\\\\ &cto include a backslash."));
            case UNCLOSED_QUOTE -> actor.error(legacyColorize("&cUnclosed quote. Make sure to close all quotes."));
            case EXPECTED_WHITESPACE ->
                    actor.error(legacyColorize("&cExpected whitespace to end one argument, but found trailing data."));
        }
    }

    @Override
    public void onInvalidListSize(@NotNull InvalidListSizeException e, @NotNull SpongeCommandActor actor, @NotNull ParameterNode<SpongeCommandActor, ?> parameter) {
        if (e.inputSize() < e.minimum())
            actor.error(legacyColorize("&cYou must input at least &e" + fmt(e.minimum()) + " &centries for &e" + parameter.name() + "&c."));
        if (e.inputSize() > e.maximum())
            actor.error(legacyColorize("&cYou must input at most &e" + fmt(e.maximum()) + " &centries for &e" + parameter.name() + "&c."));
    }

    @Override public void onInvalidBoolean(@NotNull InvalidBooleanException e, @NotNull SpongeCommandActor actor) {
        actor.error(legacyColorize("&cExpected &etrue &cor &efalse&c, found &e" + e.input() + "&c."));
    }

    @Override public void onInvalidDecimal(@NotNull InvalidDecimalException e, @NotNull SpongeCommandActor actor) {
        actor.error(legacyColorize("&cInvalid number: &e" + e.input() + "&c."));
    }

    @Override public void onInvalidInteger(@NotNull InvalidIntegerException e, @NotNull SpongeCommandActor actor) {
        actor.error(legacyColorize("&cInvalid integer: &e" + e.input() + "&c."));
    }

    @Override public void onInvalidUUID(@NotNull InvalidUUIDException e, @NotNull SpongeCommandActor actor) {
        actor.error(legacyColorize("&cInvalid UUID: " + e.input() + "&c."));
    }

    @Override
    public void onMissingArgument(@NotNull MissingArgumentException e, @NotNull SpongeCommandActor actor, @NotNull ParameterNode<SpongeCommandActor, ?> parameter) {
        actor.error(legacyColorize("&cRequired parameter is missing: &e" + parameter.name() + "&c."));
    }

    @Override public void onNoPermission(@NotNull NoPermissionException e, @NotNull SpongeCommandActor actor) {
        actor.error(legacyColorize("&cYou do not have permission to execute this command!"));
    }

    @HandleException
    public void onMalformedEntitySelector(MalformedSelectorException e, @NotNull SpongeCommandActor actor) {
        actor.error(legacyColorize("&cMalformed entity selector: &e" + e.input() + "&c. Error: &e" + e.errorMessage()));
    }

    @Override
    public void onNumberNotInRange(@NotNull NumberNotInRangeException e, @NotNull SpongeCommandActor actor, @NotNull ParameterNode<SpongeCommandActor, Number> parameter) {
        if (e.input().doubleValue() < e.minimum())
            actor.error(legacyColorize("&c" + parameter.name() + " too small &e(" + fmt(e.input()) + ")&c. Must be at least &e" + fmt(e.minimum()) + "&c."));
        if (e.input().doubleValue() > e.maximum())
            actor.error(legacyColorize("&c" + parameter.name() + " too large &e(" + fmt(e.input()) + ")&c. Must be at most &e" + fmt(e.maximum()) + "&c."));
    }

    @Override public void onUnknownCommand(@NotNull UnknownCommandException e, @NotNull SpongeCommandActor actor) {
        actor.error(legacyColorize("Unknown command: &e" + e.input() + "&c."));
    }
}
