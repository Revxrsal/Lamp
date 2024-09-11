package revxrsal.commands.minestom.exception;

import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.*;
import revxrsal.commands.minestom.actor.MinestomCommandActor;
import revxrsal.commands.node.ParameterNode;

import static revxrsal.commands.minestom.util.MinestomUtils.legacyColorize;

public class MinestomExceptionHandler extends DefaultExceptionHandler<MinestomCommandActor> {

    @HandleException
    public void onInvalidPlayer(InvalidPlayerException e, MinestomCommandActor actor) {
        actor.error(legacyColorize("&cInvalid player: &e" + e.input() + "&c."));
    }

    @HandleException
    public void onInvalidInstance(InvalidInstanceException e, MinestomCommandActor actor) {
        actor.error(legacyColorize("&cInvalid instance: &e" + e.input() + "&c."));
    }

    @HandleException
    public void onArgumentSyntaxException(ArgumentSyntaxException e, MinestomCommandActor actor) {
        actor.error(e.getMessage());
    }

    @HandleException
    public void onSenderNotPlayer(SenderNotPlayerException e, MinestomCommandActor actor) {
        actor.error(legacyColorize("&cYou must be a player to execute this command!"));
    }

    @HandleException
    public void onSenderNotConsole(SenderNotConsoleException e, MinestomCommandActor actor) {
        actor.error(legacyColorize("&cYou must be the console to execute this command!"));
    }

    @Override public void onEnumNotFound(@NotNull EnumNotFoundException e, @NotNull MinestomCommandActor actor) {
        actor.error(legacyColorize("&cInvalid choice: &e" + e.input() + "&c. Please enter a valid option from the available values."));
    }

    @Override public void onExpectedLiteral(@NotNull ExpectedLiteralException e, @NotNull MinestomCommandActor actor) {
        actor.error(legacyColorize("&cExpected &e" + e.node().name() + "&c, found &e" + e.input() + "&c."));
    }

    @Override public void onInputParse(@NotNull InputParseException e, @NotNull MinestomCommandActor actor) {
        switch (e.cause()) {
            case INVALID_ESCAPE_CHARACTER ->
                    actor.error(legacyColorize("&cInvalid input. Use &e\\\\ &cto include a backslash."));
            case UNCLOSED_QUOTE -> actor.error(legacyColorize("&cUnclosed quote. Make sure to close all quotes."));
            case EXPECTED_WHITESPACE ->
                    actor.error(legacyColorize("&cExpected whitespace to end one argument, but found trailing data."));
        }
    }

    @Override
    public void onInvalidListSize(@NotNull InvalidListSizeException e, @NotNull MinestomCommandActor actor, @NotNull ParameterNode<MinestomCommandActor, ?> parameter) {
        if (e.inputSize() < e.minimum())
            actor.error(legacyColorize("&cYou must input at least &e" + fmt(e.minimum()) + " &centries for &e" + parameter.name() + "&c."));
        if (e.inputSize() > e.maximum())
            actor.error(legacyColorize("&cYou must input at most &e" + fmt(e.maximum()) + " &centries for &e" + parameter.name() + "&c."));
    }

    @Override
    public void onInvalidStringSize(@NotNull InvalidStringSizeException e, @NotNull MinestomCommandActor actor, @NotNull ParameterNode<MinestomCommandActor, ?> parameter) {
        if (e.input().length() < e.minimum())
            actor.error(legacyColorize("&cParameter &e" + parameter.name() + " &cmust be at least &e" + fmt(e.minimum()) + " &ccharacters long."));
        if (e.input().length() > e.maximum())
            actor.error(legacyColorize("&cParameter &e" + parameter.name() + " &ccan be at most &e" + fmt(e.maximum()) + " &ccharacters long."));
    }

    @Override public void onInvalidBoolean(@NotNull InvalidBooleanException e, @NotNull MinestomCommandActor actor) {
        actor.error(legacyColorize("&cExpected &etrue &cor &efalse&c, found &e" + e.input() + "&c."));
    }

    @Override public void onInvalidDecimal(@NotNull InvalidDecimalException e, @NotNull MinestomCommandActor actor) {
        actor.error(legacyColorize("&cInvalid number: &e" + e.input() + "&c."));
    }

    @Override public void onInvalidInteger(@NotNull InvalidIntegerException e, @NotNull MinestomCommandActor actor) {
        actor.error(legacyColorize("&cInvalid integer: &e" + e.input() + "&c."));
    }

    @Override public void onInvalidUUID(@NotNull InvalidUUIDException e, @NotNull MinestomCommandActor actor) {
        actor.error(legacyColorize("&cInvalid UUID: " + e.input() + "&c."));
    }

    @Override
    public void onMissingArgument(@NotNull MissingArgumentException e, @NotNull MinestomCommandActor actor, @NotNull ParameterNode<MinestomCommandActor, ?> parameter) {
        actor.error(legacyColorize("&cRequired parameter is missing: &e" + parameter.name() + "&c."));
    }

    @Override public void onNoPermission(@NotNull NoPermissionException e, @NotNull MinestomCommandActor actor) {
        actor.error(legacyColorize("&cYou do not have permission to execute this command!"));
    }

    @Override
    public void onNumberNotInRange(@NotNull NumberNotInRangeException e, @NotNull MinestomCommandActor actor, @NotNull ParameterNode<MinestomCommandActor, Number> parameter) {
        if (e.input().doubleValue() < e.minimum())
            actor.error(legacyColorize("&c" + parameter.name() + " too small &e(" + fmt(e.input()) + ")&c. Must be at least &e" + fmt(e.minimum()) + "&c."));
        if (e.input().doubleValue() > e.maximum())
            actor.error(legacyColorize("&c" + parameter.name() + " too large &e(" + fmt(e.input()) + ")&c. Must be at most &e" + fmt(e.maximum()) + "&c."));
    }

    @Override public void onInvalidHelpPage(@NotNull InvalidHelpPageException e, @NotNull MinestomCommandActor actor) {
        if (e.numberOfPages() == 1)
            actor.error(legacyColorize("Invalid help page: &e" + e.page() + "&c. Must be 1."));
        else
            actor.error(legacyColorize("Invalid help page: &e" + e.page() + "&c. Must be between &e1 &cand &e" + e.numberOfPages()));
    }

    @Override
    public void onCommandInvocation(@NotNull CommandInvocationException e, @NotNull MinestomCommandActor actor) {
        actor.error(legacyColorize("&cAn error has occurred while executing this command. Please contact the developers. Errors have been printed to the console."));
        e.cause().printStackTrace();
    }

    @Override public void onUnknownCommand(@NotNull UnknownCommandException e, @NotNull MinestomCommandActor actor) {
        actor.error(legacyColorize("&cUnknown command: &e" + e.input() + "&c."));
    }
}
