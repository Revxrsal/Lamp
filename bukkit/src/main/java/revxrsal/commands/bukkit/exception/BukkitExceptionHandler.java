package revxrsal.commands.bukkit.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.exception.*;
import revxrsal.commands.node.ParameterNode;

import static revxrsal.commands.bukkit.util.BukkitUtils.legacyColorize;

public class BukkitExceptionHandler extends DefaultExceptionHandler<BukkitCommandActor> {

    @HandleException
    public void onInvalidPlayer(InvalidPlayerException e, BukkitCommandActor actor) {
        actor.error(legacyColorize("&cInvalid player: &e" + e.input() + "&c."));
    }

    @HandleException
    public void onInvalidPlayer(InvalidWorldException e, BukkitCommandActor actor) {
        actor.error(legacyColorize("&cInvalid world: &e" + e.input() + "&c."));
    }

    @HandleException
    public void onSenderNotConsole(SenderNotConsoleException e, BukkitCommandActor actor) {
        actor.error(legacyColorize("&cYou must be the console to execute this command!"));
    }

    @HandleException
    public void onSenderNotPlayer(SenderNotPlayerException e, BukkitCommandActor actor) {
        actor.error(legacyColorize("&cYou must be a player to execute this command!"));
    }

    @HandleException
    public void onMalformedEntitySelector(MalformedEntitySelectorException e, BukkitCommandActor actor) {
        actor.error(legacyColorize("&cMalformed entity selector: &e" + e.input() + "&c. Error: &e" + e.errorMessage()));
    }

    @HandleException
    public void onNonPlayerEntities(NonPlayerEntitiesException e, BukkitCommandActor actor) {
        actor.error(legacyColorize("&cYour entity selector (&e" + e.input() + "&c) only allows players, but it contains non-player entities too."));
    }

    @Override public void onEnumNotFound(@NotNull EnumNotFoundException e, @NotNull BukkitCommandActor actor) {
        actor.error(legacyColorize("&cInvalid choice: &e" + e.input() + "&c. Please enter a valid option from the available values."));
    }

    @Override public void onExpectedLiteral(@NotNull ExpectedLiteralException e, @NotNull BukkitCommandActor actor) {
        actor.error(legacyColorize("&cExpected &e" + e.node().name() + "&c, found &e" + e.input() + "&c."));
    }

    @Override public void onInputParse(@NotNull InputParseException e, @NotNull BukkitCommandActor actor) {
        switch (e.cause()) {
            case INVALID_ESCAPE_CHARACTER ->
                    actor.error(legacyColorize("&cInvalid input. Use &e\\\\ &cto include a backslash."));
            case UNCLOSED_QUOTE -> actor.error(legacyColorize("&cUnclosed quote. Make sure to close all quotes."));
            case EXPECTED_WHITESPACE ->
                    actor.error(legacyColorize("&cExpected whitespace to end one argument, but found trailing data."));
        }
    }

    @Override
    public void onInvalidListSize(@NotNull InvalidListSizeException e, @NotNull BukkitCommandActor actor, @NotNull ParameterNode<BukkitCommandActor, ?> parameter) {
        if (e.inputSize() < e.minimum())
            actor.error(legacyColorize("&cYou must input at least &e" + fmt(e.minimum()) + " &centries for &e" + parameter.name() + "&c."));
        if (e.inputSize() > e.maximum())
            actor.error(legacyColorize("&cYou must input at most &e" + fmt(e.maximum()) + " &centries for &e" + parameter.name() + "&c."));
    }

    @Override
    public void onInvalidStringSize(@NotNull InvalidStringSizeException e, @NotNull BukkitCommandActor actor, @NotNull ParameterNode<BukkitCommandActor, ?> parameter) {
        if (e.input().length() < e.minimum())
            actor.error(legacyColorize("&cParameter &e" + parameter.name() + " &cmust be at least &e" + fmt(e.minimum()) + " &ccharacters long."));
        if (e.input().length() > e.maximum())
            actor.error(legacyColorize("&cParameter &e" + parameter.name() + " &ccan be at most &e" + fmt(e.maximum()) + " &ccharacters long."));
    }

    @Override public void onInvalidBoolean(@NotNull InvalidBooleanException e, @NotNull BukkitCommandActor actor) {
        actor.error(legacyColorize("&cExpected &etrue &cor &efalse&c, found &e" + e.input() + "&c."));
    }

    @Override public void onInvalidDecimal(@NotNull InvalidDecimalException e, @NotNull BukkitCommandActor actor) {
        actor.error(legacyColorize("&cInvalid number: &e" + e.input() + "&c."));
    }

    @Override public void onInvalidInteger(@NotNull InvalidIntegerException e, @NotNull BukkitCommandActor actor) {
        actor.error(legacyColorize("&cInvalid integer: &e" + e.input() + "&c."));
    }

    @Override public void onInvalidUUID(@NotNull InvalidUUIDException e, @NotNull BukkitCommandActor actor) {
        actor.error(legacyColorize("&cInvalid UUID: " + e.input() + "&c."));
    }

    @Override
    public void onMissingArgument(@NotNull MissingArgumentException e, @NotNull BukkitCommandActor actor, @NotNull ParameterNode<BukkitCommandActor, ?> parameter) {
        actor.error(legacyColorize("&cRequired parameter is missing: &e" + parameter.name() + "&c."));
    }

    @Override public void onNoPermission(@NotNull NoPermissionException e, @NotNull BukkitCommandActor actor) {
        actor.error(legacyColorize("&cYou do not have permission to execute this command!"));
    }

    @Override
    public void onNumberNotInRange(@NotNull NumberNotInRangeException e, @NotNull BukkitCommandActor actor, @NotNull ParameterNode<BukkitCommandActor, Number> parameter) {
        if (e.input().doubleValue() < e.minimum())
            actor.error(legacyColorize("&c" + parameter.name() + " too small &e(" + fmt(e.input()) + ")&c. Must be at least &e" + fmt(e.minimum()) + "&c."));
        if (e.input().doubleValue() > e.maximum())
            actor.error(legacyColorize("&c" + parameter.name() + " too large &e(" + fmt(e.input()) + ")&c. Must be at most &e" + fmt(e.maximum()) + "&c."));
    }

    @Override public void onUnknownCommand(@NotNull UnknownCommandException e, @NotNull BukkitCommandActor actor) {
        actor.error(legacyColorize("Unknown command: &e" + e.input() + "&c."));
    }
}
