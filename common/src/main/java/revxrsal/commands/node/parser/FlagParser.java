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
package revxrsal.commands.node.parser;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.InputParseException;
import revxrsal.commands.exception.UnknownParameterException;
import revxrsal.commands.exception.context.ErrorContext;
import revxrsal.commands.node.MutableExecutionContext;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;
import revxrsal.commands.util.Strings.StringRange;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static revxrsal.commands.node.DispatcherSettings.LONG_FORMAT_PREFIX;
import static revxrsal.commands.node.DispatcherSettings.SHORT_FORMAT_PREFIX;
import static revxrsal.commands.util.Collections.filter;
import static revxrsal.commands.util.Strings.removeRanges;

final class FlagParser<A extends CommandActor> {

    private final MutableExecutionContext<A> context;
    private final List<ParameterNode<A, Object>> parametersLeft;
    private final MutableStringStream input;
    private final List<StringRange> rangesToRemove = new ArrayList<>();

    private Throwable error;
    private ErrorContext<A> errorContext;

    public FlagParser(MutableExecutionContext<A> context, MutableStringStream input) {
        this.context = context;
        this.parametersLeft = filter(
                context.command().parameters().values(),
                parameter -> parameter.isSwitch() || parameter.isFlag()
        );
        this.input = input;
    }

    // important note: every exception must set an errorContext manually.
    public boolean tryParse() {
        try {
            while (input.hasRemaining()) {
                int start = input.position();
                if (input.peek() == ' ')
                    input.moveForward();
                String next = input.peekUnquotedString();
                if (next.startsWith(LONG_FORMAT_PREFIX)) {
                    String flagName = next.substring(LONG_FORMAT_PREFIX.length());
                    ParameterNode<A, Object> parameter = removeParameterNamed(flagName);
                    input.readUnquotedString();
                    parseNext(context, parameter);
                    int end = input.position();
                    rangesToRemove.add(new StringRange(start, end));
                } else if (next.startsWith(SHORT_FORMAT_PREFIX)) {
                    input.readUnquotedString();
                    char[] flags = next.substring(SHORT_FORMAT_PREFIX.length()).toCharArray();
                    for (char flag : flags) {
                        ParameterNode<A, Object> parameter = removeParameterWithShorthand(flag);
                        parseNext(context, parameter);
                    }
                    int end = input.position();
                    rangesToRemove.add(new StringRange(start, end));
                } else {
                    input.moveForward(next.length());
                }
            }
            for (ParameterNode<A, Object> parameter : parametersLeft) {
                if (parameter.isSwitch())
                    context.addResolvedArgument(parameter.name(), false);
                else if (parameter.isFlag()) {
                    parseFlag(context, parameter, StringStream.createMutable(""));
                }
            }
            return true;
        } catch (Throwable t) {
            error = t;
            return false;
        }
    }

    private void parseNext(MutableExecutionContext<A> context, ParameterNode<A, Object> parameter) {
        if (parameter.isSwitch()) {
            context.addResolvedArgument(parameter.name(), true);
        } else {
            if (input.hasFinished() || input.peek() != ' ')
                throw new InputParseException(InputParseException.Cause.EXPECTED_WHITESPACE);
            input.moveForward();
            parseFlag(context, parameter, input);
        }
    }

    private <T> void parseFlag(
            MutableExecutionContext<A> context,
            ParameterNode<A, T> parameter,
            MutableStringStream input
    ) {
        try {
            T value = parameter.parse(input, context);
            context.addResolvedArgument(parameter.name(), value);
        } catch (Throwable t) {
            errorContext = ErrorContext.parsingParameter(context, parameter, input);
            throw t;
        }
    }

    private @NotNull ParameterNode<A, Object> removeParameterWithShorthand(char c) {
        for (Iterator<ParameterNode<A, Object>> iterator = parametersLeft.iterator(); iterator.hasNext(); ) {
            ParameterNode<A, Object> value = iterator.next();
            Character shorthand = value.shorthand();
            if (shorthand != null && shorthand == c) {
                iterator.remove();
                return value;
            }
        }
        errorContext = ErrorContext.unknownParameter(context);
        throw new UnknownParameterException(Character.toString(c), true);
    }

    private @NotNull ParameterNode<A, Object> removeParameterNamed(String name) {
        for (Iterator<ParameterNode<A, Object>> iterator = parametersLeft.iterator(); iterator.hasNext(); ) {
            ParameterNode<A, Object> value = iterator.next();
            if (value.isFlag() && Objects.equals(value.flagName(), name)) {
                iterator.remove();
                return value;
            }
            if (value.isSwitch() && Objects.equals(value.switchName(), name)) {
                iterator.remove();
                return value;
            }
        }
        errorContext = ErrorContext.unknownParameter(context);
        throw new UnknownParameterException(name, false);
    }

    public @NotNull MutableStringStream strippedInput() {
        String string = removeRanges(input.source(), rangesToRemove);
        return StringStream.createMutable(string);
    }

    public ErrorContext<A> errorContext() {
        return errorContext;
    }

    public Throwable error() {
        return error;
    }
}
