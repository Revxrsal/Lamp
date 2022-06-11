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
import revxrsal.commands.command.ArgumentStack
import revxrsal.commands.command.CommandActor
import revxrsal.commands.command.trait.CommandAnnotationHolder
import revxrsal.commands.command.trait.PermissionHolder
import revxrsal.commands.core.CommandPath
import revxrsal.commands.exception.CommandErrorException
import revxrsal.commands.exception.SendMessageException
import revxrsal.commands.process.ParameterResolver.ParameterResolverContext
import revxrsal.commands.process.ValueResolver.ValueResolverContext
import revxrsal.commands.util.Strings
import java.util.*

inline fun <reified T : Annotation> CommandAnnotationHolder.hasAnnotation(): Boolean {
    return hasAnnotation(T::class.java)
}

inline fun <reified T : Annotation> CommandAnnotationHolder.getAnnotation(): T? {
    return getAnnotation(T::class.java)
}

fun commandError(message: String): Nothing {
    throw CommandErrorException(message)
}

fun returnWithMessage(message: String): Nothing {
    throw SendMessageException(message)
}

operator fun CommandHandler.plusAssign(instance: Any) {
    register(instance)
}

operator fun CommandHandler.minusAssign(path: CommandPath) {
    unregister(path)
}

operator fun CommandHandler.minusAssign(path: String) {
    unregister(path)
}

/**
 * A shorter way for Kotlin's ABC::class.java syntax, to be used
 * as classOf<ABC>()
 */
inline fun <reified T> classOf() = T::class.java

fun pathOf(vararg values: String) = CommandPath.get(*values)

inline val ParameterResolverContext.input: List<String>
    get() = input()

inline val ValueResolverContext.arguments: ArgumentStack
    get() = arguments()

inline val ParameterResolverContext.command
    get() = command()

inline val ParameterResolverContext.actor
    get() = actor<CommandActor>()

inline val ParameterResolverContext.parameter
    get() = parameter()

inline val ParameterResolverContext.commandHandler
    get() = commandHandler()

inline fun <reified T : CommandActor> CommandActor.getAs() = this as T

fun String.colorize(): String = Strings.colorize(this)

fun String.splitBySpace(): LinkedList<String> = Strings.splitBySpace(this)

infix fun CommandActor.canAccess(permissionHolder: PermissionHolder) = permissionHolder.hasPermission(this)