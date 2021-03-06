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
package revxrsal.commands.bukkit

import com.mojang.brigadier.arguments.ArgumentType
import org.bukkit.plugin.Plugin
import revxrsal.commands.bukkit.brigadier.ArgumentTypeResolver
import revxrsal.commands.command.CommandActor
import revxrsal.commands.command.CommandParameter

/**
 * Creates a [BukkitCommandHandler] and applies the given block to it
 */
inline fun Plugin.bukkitCommandHandler(crossinline block: BukkitCommandHandler.() -> Unit): BukkitCommandHandler {
    return BukkitCommandHandler.create(this).also(block)
}

/**
 * Returns the sender of this command actor
 */
inline val CommandActor.sender get() = (this as BukkitCommandActor).sender

/**
 * Returns the sender of this command actor as a player (otherwise throws
 * a [revxrsal.commands.bukkit.exception.SenderNotPlayerException])
 */
inline val CommandActor.player get() = (this as BukkitCommandActor).requirePlayer()

/**
 * Returns the sender of this command actor as a player, otherwise returns `null`.
 */
inline val CommandActor.playerOrNull get() = (this as BukkitCommandActor).asPlayer

/**
 * Applies the given block to [BukkitCommandHandler.getBrigadier] if it is present.
 */
inline fun BukkitCommandHandler.brigadier(crossinline block: BukkitBrigadier.() -> Unit) {
    brigadier.ifPresent { block(it) }
}

/**
 * Binds the given type to an argument type resolver
 */
inline fun <reified T> BukkitBrigadier.bind(resolver: ArgumentTypeResolver) {
    bind(T::class.java, resolver)
}

/**
 * Binds the given type to an argument type resolver
 */
inline fun <reified T> BukkitBrigadier.bind(crossinline resolver: (CommandParameter) -> ArgumentType<*>?) {
    bind(T::class.java, ArgumentTypeResolver { resolver(it) })
}

/**
 * Binds the given type to an argument type
 */
inline fun <reified T> BukkitBrigadier.bind(resolver: ArgumentType<*>) {
    bind(T::class.java, resolver)
}