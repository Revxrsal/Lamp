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
package revxrsal.commands.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.stream.StringStream;
import revxrsal.commands.util.Classes;

import java.util.LinkedHashMap;
import java.util.Map;

import static revxrsal.commands.util.Preconditions.notNull;

class BasicExecutionContext<A extends CommandActor> implements ExecutionContext<A> {
    protected final ExecutableCommand<A> command;
    protected final StringStream input;
    protected final A actor;
    protected final Map<String, Object> resolvedArguments = new LinkedHashMap<>();

    public BasicExecutionContext(ExecutableCommand<A> command, StringStream input, A actor) {
        this.command = command;
        this.input = input;
        this.actor = actor;
    }

    @Override public @NotNull A actor() {
        return actor;
    }

    @Override public @NotNull Lamp<A> lamp() {
        return command.lamp();
    }

    @Override public @NotNull ExecutableCommand<A> command() {
        return command;
    }

    @Override public @NotNull @UnmodifiableView Map<String, Object> resolvedArguments() {
        return resolvedArguments;
    }

    @Override public @NotNull StringStream input() {
        return input;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getResolvedArgumentOrNull(@NotNull Class<T> argumentType) {
        notNull(argumentType, "argument type");
        argumentType = Classes.wrap(argumentType);
        for (Object value : resolvedArguments.values()) {
            if (value == null)
                continue;
            if (argumentType.isAssignableFrom(value.getClass()))
                return (T) value;
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getResolvedArgumentOrNull(@NotNull String argumentName) {
        notNull(argumentName, "argument name");
        return (T) resolvedArguments.get(argumentName);
    }
}
