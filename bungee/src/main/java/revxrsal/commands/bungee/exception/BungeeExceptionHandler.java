package revxrsal.commands.bungee.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bungee.actor.BungeeCommandActor;
import revxrsal.commands.exception.*;
import revxrsal.commands.node.ParameterNode;

import static revxrsal.commands.bungee.util.BungeeUtils.legacyColorize;

public class BungeeExceptionHandler extends DefaultExceptionHandler<BungeeCommandActor> {

    @HandleException
    public void onInvalidPlayer(InvalidPlayerException e, BungeeCommandActor actor) {
        actor.error(legacyColorize("&cInvalid player: &e" + e.input() + "&c."));
    }

    @HandleException
    public void onSenderNotPlayer(SenderNotPlayerException e, BungeeCommandActor actor) {
        actor.error(legacyColorize("&cYou must be a player to execute this command!"));
    }

    @Override public void onEnumNotFound(@NotNull EnumNotFoundException e, @NotNull BungeeCommandActor actor) {
        actor.error(legacyColorize("&cInvalid choice: &e" + e.input() + "&c. Please enter a valid option from the available values."));
    }

    @Override public void onExpectedLiteral(@NotNull ExpectedLiteralException e, @NotNull BungeeCommandActor actor) {
        actor.error(legacyColorize("&cExpected &e" + e.node().name() + "&c, found &e" + e.input() + "&c."));
    }

    @Override public void onInputParse(@NotNull InputParseException e, @NotNull BungeeCommandActor actor) {
        switch (e.cause()) {
            case INVALID_ESCAPE_CHARACTER ->
                    actor.error(legacyColorize("&cInvalid input. Use &e\\\\ &cto include a backslash."));
            case UNCLOSED_QUOTE -> actor.error(legacyColorize("&cUnclosed quote. Make sure to close all quotes."));
            case EXPECTED_WHITESPACE ->
                    actor.error(legacyColorize("&cExpected whitespace to end one argument, but found trailing data."));
        }
    }

    @Override
    public void onInvalidListSize(@NotNull InvalidListSizeException e, @NotNull BungeeCommandActor actor, @NotNull ParameterNode<BungeeCommandActor, ?> parameter) {
        if (e.inputSize() < e.minimum())
            actor.error(legacyColorize("&cYou must input at least &e" + fmt(e.minimum()) + " &centries for &e" + parameter.name() + "&c."));
        if (e.inputSize() > e.maximum())
            actor.error(legacyColorize("&cYou must input at most &e" + fmt(e.maximum()) + " &centries for &e" + parameter.name() + "&c."));
    }

    @Override
    public void onInvalidStringSize(@NotNull InvalidStringSizeException e, @NotNull BungeeCommandActor actor, @NotNull ParameterNode<BungeeCommandActor, ?> parameter) {
        if (e.input().length() < e.minimum())
            actor.error(legacyColorize("&cParameter &e" + parameter.name() + " &cmust be at least &e" + fmt(e.minimum()) + " &ccharacters long."));
        if (e.input().length() > e.maximum())
            actor.error(legacyColorize("&cParameter &e" + parameter.name() + " &ccan be at most &e" + fmt(e.maximum()) + " &ccharacters long."));
    }

    @Override public void onInvalidBoolean(@NotNull InvalidBooleanException e, @NotNull BungeeCommandActor actor) {
        actor.error(legacyColorize("&cExpected &etrue &cor &efalse&c, found &e" + e.input() + "&c."));
    }

    @Override public void onInvalidDecimal(@NotNull InvalidDecimalException e, @NotNull BungeeCommandActor actor) {
        actor.error(legacyColorize("&cInvalid number: &e" + e.input() + "&c."));
    }

    @Override public void onInvalidInteger(@NotNull InvalidIntegerException e, @NotNull BungeeCommandActor actor) {
        actor.error(legacyColorize("&cInvalid integer: &e" + e.input() + "&c."));
    }

    @Override public void onInvalidUUID(@NotNull InvalidUUIDException e, @NotNull BungeeCommandActor actor) {
        actor.error(legacyColorize("&cInvalid UUID: " + e.input() + "&c."));
    }

    @Override
    public void onMissingArgument(@NotNull MissingArgumentException e, @NotNull BungeeCommandActor actor, @NotNull ParameterNode<BungeeCommandActor, ?> parameter) {
        actor.error(legacyColorize("&cRequired parameter is missing: &e" + parameter.name() + "&c. Usage: &e/" + parameter.command().usage() + "&c."));
    }

    @Override public void onNoPermission(@NotNull NoPermissionException e, @NotNull BungeeCommandActor actor) {
        actor.error(legacyColorize("&cYou do not have permission to execute this command!"));
    }

    @Override
    public void onNumberNotInRange(@NotNull NumberNotInRangeException e, @NotNull BungeeCommandActor actor, @NotNull ParameterNode<BungeeCommandActor, Number> parameter) {
        if (e.input().doubleValue() < e.minimum())
            actor.error(legacyColorize("&c" + parameter.name() + " too small &e(" + fmt(e.input()) + ")&c. Must be at least &e" + fmt(e.minimum()) + "&c."));
        if (e.input().doubleValue() > e.maximum())
            actor.error(legacyColorize("&c" + parameter.name() + " too large &e(" + fmt(e.input()) + ")&c. Must be at most &e" + fmt(e.maximum()) + "&c."));
    }

    @Override public void onInvalidHelpPage(@NotNull InvalidHelpPageException e, @NotNull BungeeCommandActor actor) {
        if (e.numberOfPages() == 1)
            actor.error(legacyColorize("Invalid help page: &e" + e.page() + "&c. Must be 1."));
        else
            actor.error(legacyColorize("Invalid help page: &e" + e.page() + "&c. Must be between &e1 &cand &e" + e.numberOfPages()));
    }

    @Override public void onUnknownCommand(@NotNull UnknownCommandException e, @NotNull BungeeCommandActor actor) {
        actor.error(legacyColorize("&cUnknown command: &e" + e.input() + "&c."));
    }
}
