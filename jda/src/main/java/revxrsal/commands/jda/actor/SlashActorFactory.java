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
package revxrsal.commands.jda.actor;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;

/**
 * Represents an interface that constructs instances of a custom
 * actor based on different JDA events.
 *
 * @param <A> The actor type
 */
public interface SlashActorFactory<A extends SlashCommandActor> {

    /**
     * Returns the default {@link SlashActorFactory} that returns a simple
     * {@link SlashCommandActor} implementation
     *
     * @return The default {@link SlashActorFactory}.
     */
    static @NotNull SlashActorFactory<SlashCommandActor> defaultFactory() {
        return BasicActorFactory.INSTANCE;
    }

    /**
     * Creates a new actor based on a user executing a slash command.
     *
     * @param event The event
     * @param lamp  The {@link Lamp} instance
     * @return The newly created actor
     */
    @NotNull A create(@NotNull SlashCommandInteractionEvent event, @NotNull Lamp<A> lamp);

    /**
     * Creates a new actor based on a user auto-completing a slash command.
     *
     * @param event The event
     * @param lamp  The {@link Lamp} instance
     * @return The newly created actor
     */
    @NotNull A create(@NotNull CommandAutoCompleteInteractionEvent event, @NotNull Lamp<A> lamp);

}
