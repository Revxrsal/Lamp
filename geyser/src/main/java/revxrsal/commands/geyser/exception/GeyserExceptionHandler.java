/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.geyser.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.*;
import revxrsal.commands.geyser.actor.GeyserCommandActor;
import revxrsal.commands.node.ParameterNode;

import static revxrsal.commands.geyser.util.GeyserUtils.legacyColorize;

public class GeyserExceptionHandler extends DefaultExceptionHandler<GeyserCommandActor> {

    @HandleException
    public void onInvalidPlayer(@NotNull InvalidPlayerException e, @NotNull GeyserCommandActor actor) {
        actor.error(legacyColorize("&cInvalid player: &e" + e.input() + "&c."));
    }

    @HandleException
    public void onSenderNotBedrockPlayer(@NotNull SenderNotBedrockPlayerException e, @NotNull GeyserCommandActor actor) {
        actor.error(legacyColorize("&cYou must be a Bedrock player to execute this command!"));
    }

    @HandleException
    public void onSenderNotConsole(@NotNull SenderNotConsoleException e, @NotNull GeyserCommandActor actor) {
        actor.error(legacyColorize("&cYou must be the console to execute this command!"));
    }

    @Override public void onEnumNotFound(@NotNull EnumNotFoundException e, @NotNull GeyserCommandActor actor) {
        actor.error(legacyColorize("&cInvalid choice: &e" + e.input() + "&c. Please enter a valid option from the available values."));
    }

    @Override public void onExpectedLiteral(@NotNull ExpectedLiteralException e, @NotNull GeyserCommandActor actor) {
        actor.error(legacyColorize("&cExpected &e" + e.node().name() + "&c, found &e" + e.input() + "&c."));
    }

    @Override public void onInputParse(@NotNull InputParseException e, @NotNull GeyserCommandActor actor) {
        actor.error(legacyColorize(switch (e.cause()) {
            case INVALID_ESCAPE_CHARACTER -> "&cInvalid input. Use &e\\\\ &cto include a backslash.";
            case UNCLOSED_QUOTE -> "&cUnclosed quote. Make sure to close all quotes.";
            case EXPECTED_WHITESPACE -> "&cExpected whitespace to end one argument, but found trailing data.";
        }));
    }

    @Override
    public void onInvalidListSize(@NotNull InvalidListSizeException e, @NotNull GeyserCommandActor actor, @NotNull ParameterNode<GeyserCommandActor, ?> parameter) {
        if (e.inputSize() < e.minimum())
            actor.error(legacyColorize("&cYou must input at least &e" + fmt(e.minimum()) + " &centries for &e" + parameter.name() + "&c."));
        if (e.inputSize() > e.maximum())
            actor.error(legacyColorize("&cYou must input at most &e" + fmt(e.maximum()) + " &centries for &e" + parameter.name() + "&c."));
    }

    @Override
    public void onInvalidStringSize(@NotNull InvalidStringSizeException e, @NotNull GeyserCommandActor actor, @NotNull ParameterNode<GeyserCommandActor, ?> parameter) {
        if (e.input().length() < e.minimum())
            actor.error(legacyColorize("&cParameter &e" + parameter.name() + " &cmust be at least &e" + fmt(e.minimum()) + " &ccharacters long."));
        if (e.input().length() > e.maximum())
            actor.error(legacyColorize("&cParameter &e" + parameter.name() + " &ccan be at most &e" + fmt(e.maximum()) + " &ccharacters long."));
    }

    @Override public void onInvalidBoolean(@NotNull InvalidBooleanException e, @NotNull GeyserCommandActor actor) {
        actor.error(legacyColorize("&cExpected &etrue &cor &efalse&c, found &e" + e.input() + "&c."));
    }

    @Override public void onInvalidDecimal(@NotNull InvalidDecimalException e, @NotNull GeyserCommandActor actor) {
        actor.error(legacyColorize("&cInvalid number: &e" + e.input() + "&c."));
    }

    @Override public void onInvalidInteger(@NotNull InvalidIntegerException e, @NotNull GeyserCommandActor actor) {
        actor.error(legacyColorize("&cInvalid integer: &e" + e.input() + "&c."));
    }

    @Override public void onInvalidUUID(@NotNull InvalidUUIDException e, @NotNull GeyserCommandActor actor) {
        actor.error(legacyColorize("&cInvalid UUID: " + e.input() + "&c."));
    }

    @Override
    public void onMissingArgument(@NotNull MissingArgumentException e, @NotNull GeyserCommandActor actor, @NotNull ParameterNode<GeyserCommandActor, ?> parameter) {
        actor.error(legacyColorize("&cRequired parameter is missing: &e" + parameter.name() + "&c. Usage: &e/" + parameter.command().usage() + "&c."));
    }

    @Override public void onNoPermission(@NotNull NoPermissionException e, @NotNull GeyserCommandActor actor) {
        actor.error(legacyColorize("&cYou do not have permission to execute this command!"));
    }

    @Override
    public void onNumberNotInRange(@NotNull NumberNotInRangeException e, @NotNull GeyserCommandActor actor, @NotNull ParameterNode<GeyserCommandActor, Number> parameter) {
        if (e.input().doubleValue() < e.minimum())
            actor.error(legacyColorize("&c" + parameter.name() + " too small &e(" + fmt(e.input()) + ")&c. Must be at least &e" + fmt(e.minimum()) + "&c."));
        if (e.input().doubleValue() > e.maximum())
            actor.error(legacyColorize("&c" + parameter.name() + " too large &e(" + fmt(e.input()) + ")&c. Must be at most &e" + fmt(e.maximum()) + "&c."));
    }

    @Override public void onInvalidHelpPage(@NotNull InvalidHelpPageException e, @NotNull GeyserCommandActor actor) {
        if (e.numberOfPages() == 1)
            actor.error(legacyColorize("Invalid help page: &e" + e.page() + "&c. Must be 1."));
        else
            actor.error(legacyColorize("Invalid help page: &e" + e.page() + "&c. Must be between &e1 &cand &e" + e.numberOfPages()));
    }

    @Override
    public void onCommandInvocation(@NotNull CommandInvocationException e, @NotNull GeyserCommandActor actor) {
        actor.error(legacyColorize("&cAn error has occurred while executing this command. Please contact the developers. Errors have been printed to the console."));
        e.cause().printStackTrace();
    }

    @Override public void onValueNotAllowed(@NotNull ValueNotAllowedException e, @NotNull GeyserCommandActor actor) {
        String allowedValues = String.join("&c, &e", e.allowedValues());
        actor.error(legacyColorize("Received an invalid value: &e" + e.input() + "&c. Allowed values: &e" + allowedValues + "&c."));
    }

    @Override public void onUnknownCommand(@NotNull UnknownCommandException e, @NotNull GeyserCommandActor actor) {
        actor.error(legacyColorize("&cUnknown command: &e" + e.input() + "&c."));
    }
}
