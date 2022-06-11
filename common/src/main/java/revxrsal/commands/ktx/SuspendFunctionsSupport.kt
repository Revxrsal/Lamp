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
package revxrsal.commands.ktx

import revxrsal.commands.CommandHandler
import revxrsal.commands.CommandHandlerVisitor
import revxrsal.commands.process.ContextResolver
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A [CommandHandlerVisitor] that allows command functions to be [suspend]
 * functions. This will allow running coroutine methods inside them directly.
 *
 * This works by registering resolvers for Kotlin's [Continuation].
 *
 * To enable, use [CommandHandler.supportSuspendFunctions]
 */
object SuspendFunctionsSupport : CommandHandlerVisitor {

    override fun visit(handler: CommandHandler) {
        handler.registerContextResolverFactory {
            if (it.isLastInMethod && it.type.isAssignableFrom(Continuation::class.java))
                ContinuationResolver
            else
                null
        }
    }

    private object ContinuationResolver : ContextResolver<Continuation<Any>> {

        override fun resolve(context: ContextResolver.ContextResolverContext): Continuation<Any> {
            return BasicContinuation {
                context.commandHandler.exceptionHandler.handleException(it, context.actor())
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