/*
 * This file is part of sweeper, licensed under the MIT License.
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
package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ParameterNode;

public class DefaultExceptionHandler<A extends CommandActor> extends RuntimeExceptionAdapter<A> {

    @HandleException
    public void onEnumNotFound(@NotNull EnumNotFoundException e, @NotNull A actor) {
        actor.error("Invalid choice: '" + e.input() + "'. Please enter a valid option from the available values.");
    }

    @HandleException
    public void onExpectedLiteral(@NotNull ExpectedLiteralException e, @NotNull A actor) {
        actor.error("Expected '" + e.node().name() + "', found '" + e.input() + "'");
    }

    @HandleException
    public void onInputParse(@NotNull InputParseException e, @NotNull A actor) {
        switch (e.cause()) {
            case INVALID_ESCAPE_CHARACTER -> actor.error("Invalid input. Use \\\\ to include a backslash.");
            case UNCLOSED_QUOTE -> actor.error("Unclosed quote. Make sure to close all quotes.");
            case EXPECTED_WHITESPACE -> actor.error("Expected whitespace to end one argument, but found trailing data");
        }
    }

    @HandleException
    public void onInvalidListSize(@NotNull InvalidListSizeException e, @NotNull A actor, @NotNull ParameterNode<A, ?> parameter) {
        if (e.inputSize() < e.minimum())
            actor.error("You must input at least " + fmt(e.minimum()) + " entries for " + parameter.name());
        if (e.inputSize() > e.maximum())
            actor.error("You must input at most " + fmt(e.maximum()) + " entries for " + parameter.name());
    }

    @HandleException
    public void onInvalidStringSize(@NotNull InvalidStringSizeException e, @NotNull A actor, @NotNull ParameterNode<A, ?> parameter) {
        if (e.input().length() < e.minimum())
            actor.error("Parameter " + parameter.name() + " must be at least " + fmt(e.minimum()) + " characters long.");
        if (e.input().length() > e.maximum())
            actor.error("Parameter " + parameter.name() + " can be at most " + fmt(e.maximum()) + " characters long.");
    }

    @HandleException
    public void onInvalidBoolean(@NotNull InvalidBooleanException e, @NotNull A actor) {
        actor.error("Expected 'true' or 'false', found " + e.input());
    }

    @HandleException
    public void onInvalidDecimal(@NotNull InvalidDecimalException e, @NotNull A actor) {
        actor.error("Invalid number: " + e.input());
    }

    @HandleException
    public void onInvalidInteger(@NotNull InvalidIntegerException e, @NotNull A actor) {
        actor.error("Invalid integer: " + e.input());
    }

    @HandleException
    public void onInvalidUUID(@NotNull InvalidUUIDException e, @NotNull A actor) {
        actor.error("Invalid UUID: " + e.input());
    }

    @HandleException
    public void onMissingArgument(@NotNull MissingArgumentException e, @NotNull A actor, @NotNull ParameterNode<A, ?> parameter) {
        actor.error("Required parameter is missing: " + parameter.name());
    }

    @HandleException
    public void onNoPermission(@NotNull NoPermissionException e, @NotNull A actor) {
        actor.error("You do not have permission to execute this command!");
    }

    @HandleException
    public void onNumberNotInRange(@NotNull NumberNotInRangeException e, @NotNull A actor, @NotNull ParameterNode<A, Number> parameter) {
        if (e.input().doubleValue() < e.minimum())
            actor.error(parameter.name() + " too small (" + fmt(e.input()) + "). Must be at least " + fmt(e.minimum()));
        if (e.input().doubleValue() > e.maximum())
            actor.error(parameter.name() + " too large (" + fmt(e.input()) + "). Must be at most " + fmt(e.maximum()));
    }

    @HandleException
    public void onUnknownCommand(@NotNull UnknownCommandException e, @NotNull A actor) {
        actor.error("Unknown command: " + e.input());
    }

    @HandleException
    public void onInvalidHelpPage(@NotNull InvalidHelpPageException e, @NotNull A actor) {
        if (e.numberOfPages() == 1)
            actor.error("Invalid help page: " + e.page() + ". Must be 1.");
        else
            actor.error("Invalid help page: " + e.page() + ". Must be between 1 and " + e.numberOfPages());
    }

    @HandleException
    public void onCommandInvocation(@NotNull CommandInvocationException e, @NotNull A actor) {
        actor.error("An error has occurred while executing this command. Please contact the developers. Errors have been printed to the console.");
        e.cause().printStackTrace();
    }

    @HandleException
    public void onSendable(@NotNull SendableException e, @NotNull A actor) {
        e.sendTo(actor);
    }

}
