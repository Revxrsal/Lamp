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

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.util.Lazy;

import java.util.UUID;
import java.util.function.Supplier;

final class BasicActor implements SlashCommandActor {

    private final GenericInteractionCreateEvent event;
    private final Lamp<SlashCommandActor> lamp;
    private final Supplier<UUID> uniqueId = Lazy.of(() -> new UUID(0, user().getIdLong()));

    public BasicActor(GenericInteractionCreateEvent event, Lamp<SlashCommandActor> lamp) {
        this.event = event;
        this.lamp = lamp;
    }

    @Override public @NotNull User user() {
        return event.getUser();
    }

    @Override public @NotNull GenericInteractionCreateEvent event() {
        return event;
    }

    @Override public @NotNull String name() {
        return event.getUser().getEffectiveName();
    }

    @Override public @NotNull UUID uniqueId() {
        return uniqueId.get();
    }

    @Override public void sendRawMessage(@NotNull String message) {
        event.getMessageChannel().sendMessage(message).queue();
    }

    @Override public void sendRawError(@NotNull String message) {
        event.getMessageChannel().sendMessage("🚨 " + message).queue();
    }

    @Override public Lamp<SlashCommandActor> lamp() {
        return lamp;
    }
}
