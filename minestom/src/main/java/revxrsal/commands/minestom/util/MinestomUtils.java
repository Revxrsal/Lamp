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
package revxrsal.commands.minestom.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.MissingArgumentException;
import revxrsal.commands.minestom.actor.MinestomCommandActor;
import revxrsal.commands.node.DispatcherSettings;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.node.MutableExecutionContext;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.stream.StringStream;

import static revxrsal.commands.util.Preconditions.cannotInstantiate;
import static revxrsal.commands.util.Preconditions.notNull;

public final class MinestomUtils {

    private MinestomUtils() {
        cannotInstantiate(MinestomUtils.class);
    }

    /**
     * Returns a {@link String} that colorizes the given text using
     * the ampersand for color coding.
     *
     * @param text The text to colorize
     * @return The component
     */
    public static @NotNull Component legacyColorize(@NotNull String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    /**
     * Converts a Minestom {@link CommandContext} into a Lamp {@link ExecutionContext}
     *
     * @param <A>     The actor type
     * @param context The Minestom context
     */
    public static <A extends MinestomCommandActor> void readIntoLampContext(
            @NotNull MutableExecutionContext<A> executionContext,
            @NotNull CommandContext context
    ) {
        notNull(context, "context");
        for (ParameterNode<A, ?> parameter : executionContext.command().parameters().values()) {
            Object o;
            if (parameter.isSwitch()) {
                o = containsFlag(context, parameter.switchName());
            } else {
                o = context.get(parameter.name());
            }
            if (o != null)
                executionContext.addResolvedArgument(parameter.name(), o);
            else {
                if (parameter.isFlag() && containsFlag(context, parameter.flagName()))
                    throw new MissingArgumentException(parameter, parameter.command());
                Object def = parameter.parse(StringStream.createMutable(""), executionContext);
                executionContext.addResolvedArgument(parameter.name(), def);
            }
        }
    }

    private static boolean containsFlag(CommandContext context, String name) {
        return context.has(DispatcherSettings.LONG_FORMAT_PREFIX + name)
                || context.has(DispatcherSettings.SHORT_FORMAT_PREFIX + name);
    }

}
