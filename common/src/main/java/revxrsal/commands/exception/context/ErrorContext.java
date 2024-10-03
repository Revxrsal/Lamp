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
package revxrsal.commands.exception.context;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Flag;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.node.LiteralNode;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.stream.StringStream;

/**
 * Represents the context in which the error has occurred.
 * <p>
 * For example, if the error was because a parameter was invalid, this
 * will be a {@link ParsingParameter} context,
 * which contains information about the parameter being parsed.
 * <p>
 * If, however, it occurred during executing the command function,
 * this will be an {@link ExecutingFunction} context.
 *
 * @param <A> The actor type
 */
public interface ErrorContext<A extends CommandActor> {

    /**
     * Creates a {@link ParsingLiteral} context for the given literal node
     *
     * @param node The literal node
     * @param <A>  The actor type
     * @return The context
     */
    static <A extends CommandActor> @NotNull ParsingLiteral<A> parsingLiteral(@NotNull ExecutionContext<A> context, @NotNull LiteralNode<A> node) {
        return new ParsingLiteralContext<>(context, node);
    }

    /**
     * Creates a {@link ParsingParameter} context for the given parameter node
     *
     * @param node  The parameter node
     * @param input The problematic input
     * @param <A>   The actor type
     * @return The context
     */
    static <A extends CommandActor> @NotNull ParsingParameter<A> parsingParameter(@NotNull ExecutionContext<A> context, @NotNull ParameterNode<A, ?> node, @NotNull StringStream input) {
        return new ParsingParameterContext<>(context, node, input);
    }

    /**
     * Creates an {@link ExecutingFunction} error context
     *
     * @param <A> The actor type
     * @return The context
     */
    static <A extends CommandActor> @NotNull ExecutingFunction<A> executingFunction(@NotNull ExecutionContext<A> context) {
        return new ExecutingFunctionContext<>(context);
    }

    /**
     * Creates an {@link UnknownCommand} error context
     *
     * @param <A> The actor type
     * @return The context
     */
    static <A extends CommandActor> @NotNull UnknownCommand<A> unknownCommand(@NotNull A actor) {
        return new UnknownCommandContext<>(actor);
    }

    /**
     * Creates an {@link UnknownParameter} error context
     *
     * @param <A> The actor type
     * @return The context
     */
    static <A extends CommandActor> @NotNull UnknownParameter<A> unknownParameter(@NotNull ExecutionContext<A> context) {
        return new UnknownParameterContext<>(context);
    }

    /**
     * Tests whether this context has a {@link ExecutionContext} or not.
     * <p>
     * All error contexts have an {@link ExecutionContext} except {@link UnknownCommand},
     * which has no command to wrap (it's unknown)
     *
     * @return if this context has an {@link ExecutionContext}
     */
    default boolean hasExecutionContext() {
        return context() != null;
    }

    /**
     * Returns the {@link ExecutionContext} in this context.
     * <p>
     * All error contexts have an {@link ExecutionContext} except {@link UnknownCommand},
     * which has no command to wrap (it's unknown)
     *
     * @return the {@link ExecutionContext} or {@code null} if there's none
     */
    ExecutionContext<A> context();

    /**
     * Returns the actor
     *
     * @return The actor
     */
    @NotNull A actor();

    /**
     * Returns the Lamp instance
     *
     * @return The Lamp instance
     */
    @NotNull Lamp<A> lamp();

    /**
     * Tests whether the error occurred during parsing a literal
     *
     * @return whether the error occurred during parsing a literal
     */
    default boolean isParsingLiteral() {
        return this instanceof ParsingLiteral;
    }

    /**
     * Tests whether the error occurred during parsing a parameter
     *
     * @return whether the error occurred during parsing a parameter
     */
    default boolean isParsingParameter() {
        return this instanceof ParsingParameter;
    }

    /**
     * Tests whether the error occurred during executing a function
     *
     * @return whether the error occurred during executing a function
     */
    default boolean isExecutingFunction() {
        return this instanceof ExecutingFunctionContext;
    }

    /**
     * Represents the context for an error that occurred during parsing
     * a literal node
     *
     * @param <A> The actor type
     */
    interface ParsingLiteral<A extends CommandActor> extends ErrorContext<A> {
        @NotNull LiteralNode<A> literal();

        @Override
        @NotNull ExecutionContext<A> context();
    }

    /**
     * Represents the context for an error that occurred during parsing
     * a parameter node
     *
     * @param <A> The actor type
     */
    interface ParsingParameter<A extends CommandActor> extends ErrorContext<A> {
        @NotNull ParameterNode<A, ?> parameter();

        @Override
        @NotNull ExecutionContext<A> context();
    }

    /**
     * Represents the context for an error that occurred during executing
     * a command function
     *
     * @param <A> The actor type
     */
    interface ExecutingFunction<A extends CommandActor> extends ErrorContext<A> {
        @Override
        @NotNull ExecutionContext<A> context();
    }

    /**
     * Represents the context for an error due to an unknown command.
     *
     * @param <A> The actor type
     */
    interface UnknownCommand<A extends CommandActor> extends ErrorContext<A> {

        @Override
        @Contract("-> null")
        @Nullable
        default ExecutionContext<A> context() {
            return null;
        }
    }

    /**
     * Represents the context for an error due to an unknown parameter. This
     * is used for {@link Switch} and {@link Flag} parameters
     *
     * @param <A> The actor type
     */
    interface UnknownParameter<A extends CommandActor> extends ErrorContext<A> {

        @Override
        @NotNull ExecutionContext<A> context();
    }
}
