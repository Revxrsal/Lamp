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
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.util.Classes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static revxrsal.commands.util.Preconditions.notNull;

/**
 * Represents a basic, mutable implementation of {@link ExecutionContext}.
 *
 * @param <A> The actor type
 */
@SuppressWarnings("unchecked")
public final class BasicExecutionContext<A extends CommandActor> implements ExecutionContext<A> {

    private final Lamp<A> lamp;
    private final ExecutableCommand<A> command;
    private final A actor;
    private final Map<String, Object> resolvedArguments = new LinkedHashMap<>();

    public BasicExecutionContext(Lamp<A> lamp, ExecutableCommand<A> command, A actor) {
        this.lamp = lamp;
        this.command = command;
        this.actor = actor;
    }

    @Override
    public @NotNull A actor() {
        return actor;
    }

    @Override
    public @NotNull Lamp<A> lamp() {
        return lamp;
    }

    @Override
    public @NotNull ExecutableCommand<A> command() {
        return command;
    }

    @Override
    public @NotNull Map<String, Object> resolvedArguments() {
        return Collections.unmodifiableMap(resolvedArguments);
    }

    @Override
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
    public <T> @Nullable T getResolvedArgumentOrNull(@NotNull String argumentName) {
        notNull(argumentName, "argument name");
        return (T) resolvedArguments.get(argumentName);
    }

    public void addResoledArgument(@NotNull String name, Object result) {
        resolvedArguments.put(name, result);
    }

    public void clearResolvedArguments() {
        resolvedArguments.clear();
    }

}
