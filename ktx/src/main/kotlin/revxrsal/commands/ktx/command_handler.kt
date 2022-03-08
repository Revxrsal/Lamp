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
import revxrsal.commands.command.CommandActor
import revxrsal.commands.command.CommandParameter
import revxrsal.commands.command.CommandPermission
import revxrsal.commands.command.trait.CommandAnnotationHolder
import revxrsal.commands.process.ContextResolver
import revxrsal.commands.process.ContextResolver.ContextResolverContext
import revxrsal.commands.process.ParameterValidator
import revxrsal.commands.process.ValueResolver
import revxrsal.commands.process.ValueResolver.ValueResolverContext
import java.util.function.Supplier

/**
 * Registers a [ValueResolver] for the given type.
 */
inline fun <reified T> CommandHandler.valueResolver(
    priority: Int = Int.MAX_VALUE,
    crossinline v: (ValueResolverContext) -> T
) = apply {
    registerValueResolver(priority, T::class.java) { v(it) }
}

/**
 * Registers a [ContextResolver] for the given type.
 */
inline fun <reified T> CommandHandler.contextResolver(
    priority: Int = Int.MAX_VALUE,
    crossinline v: (ContextResolverContext) -> T
) = apply {
    registerContextResolver(priority, T::class.java) { v(it) }
}

/**
 * Registers a [revxrsal.commands.process.ValueResolverFactory] to the command handler.
 */
fun CommandHandler.valueResolverFactory(
    priority: Int = Int.MAX_VALUE,
    factory: (CommandParameter) -> ValueResolver<*>?
) = apply {
    registerValueResolverFactory(priority, factory)
}

/**
 * Registers a [revxrsal.commands.process.ContextResolverFactory] to the command handler.
 */
fun CommandHandler.contextResolverFactory(
    priority: Int = Int.MAX_VALUE,
    factory: (CommandParameter) -> ContextResolver<*>?
) = apply {
    registerContextResolverFactory(priority, factory)
}

/**
 * Registers a handling strategy for the specified exception.
 */
inline fun <reified T : Throwable> CommandHandler.handleException(
    crossinline handler: (CommandActor, T) -> Unit
) = apply {
    registerExceptionHandler(T::class.java) { actor, exception -> handler(actor, exception) }
}

/**
 * Registers a [ParameterValidator] for the given type.
 */
inline fun <reified T> CommandHandler.parameterValidator(
    crossinline validate: (
        value: T,
        parameter: CommandParameter,
        actor: CommandActor
    ) -> Unit
) = apply {
    registerParameterValidator(
        T::class.java,
        ParameterValidator { value, param, actor -> validate(value, param, actor) })
}

/**
 * Registers a dependency (accessible using the [revxrsal.commands.annotation.Dependency] annotation)
 * for the given type
 */
inline fun <reified T> CommandHandler.registerDependency(value: T) = apply {
    registerDependency(T::class.java, value)
}

/**
 * Registers a dependency (accessible using the [revxrsal.commands.annotation.Dependency] annotation)
 * for the given type
 */
inline fun <reified T> CommandHandler.registerDependency(
    crossinline supplier: () -> T
) = apply {
    registerDependency(T::class.java, Supplier { supplier() })
}

/**
 * Registers a [revxrsal.commands.process.PermissionReader] to the command handler.
 */
fun CommandHandler.permissionReader(
    reader: CommandAnnotationHolder.() -> CommandPermission
) = apply {
    registerPermissionReader(reader)
}
