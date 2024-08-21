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
package revxrsal.commands.sponge.sender;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.permission.Subject;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.SenderResolver;
import revxrsal.commands.sponge.actor.SpongeCommandActor;

public final class SpongeSenderResolver implements SenderResolver<SpongeCommandActor> {

    @Override public boolean isSenderType(@NotNull CommandParameter parameter) {
        Class<?> type = parameter.type();
        return CommandCause.class.isAssignableFrom(parameter.type())
                || Audience.class.isAssignableFrom(parameter.type())
                || Subject.class.isAssignableFrom(type);
    }

    public @NotNull Object getSender(@NotNull Class<?> customSenderType, @NotNull SpongeCommandActor actor, @NotNull ExecutableCommand<SpongeCommandActor> command) {
        if (Subject.class.isAssignableFrom(customSenderType)) {
            return actor.subject();
        } else if (Audience.class.isAssignableFrom(customSenderType)) {
            return actor.audience();
        } else if (ServerPlayer.class.isAssignableFrom(customSenderType)) {
            return actor.requirePlayer();
        } else if (SystemSubject.class.isAssignableFrom(customSenderType)) {
            return actor.requireConsole();
        }
        return actor.cause();
    }
}
