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
@file:Suppress("UNCHECKED_CAST")

package revxrsal.commands.ktx

import revxrsal.commands.Lamp
import revxrsal.commands.LampBuilderVisitor
import revxrsal.commands.command.CommandActor
import revxrsal.commands.command.CommandParameter
import revxrsal.commands.exception.context.ErrorContext
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.parameter.ContextParameter
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A [LampBuilderVisitor] that allows command functions to be [suspend]
 * functions. This will allow running coroutine methods inside them directly.
 *
 * This works by registering resolvers for Kotlin's [Continuation].
 */
object SuspendFunctionsSupport : LampBuilderVisitor<CommandActor> {

    override fun visit(builder: Lamp.Builder<CommandActor>) {
        builder.parameterTypes()
            .addContextParameterLast(
                Continuation::class.java,
                ContinuationResolver as ContextParameter<CommandActor, Continuation<*>>
            )
    }

    private object ContinuationResolver : ContextParameter<CommandActor, Continuation<Any>> {

        override fun resolve(parameter: CommandParameter, context: ExecutionContext<CommandActor>): Continuation<Any> {
            return BasicContinuation {
                context.lamp().handleException(it, ErrorContext.executingFunction(context))
            }
        }
    }

    private class BasicContinuation<T>(private val handleException: (Throwable) -> Unit) : Continuation<T> {

        override val context get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            result.fold(
                onSuccess = {},
                onFailure = { handleException(it) }
            )
        }
    }
}