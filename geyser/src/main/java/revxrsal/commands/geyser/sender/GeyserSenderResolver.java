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
package revxrsal.commands.geyser.sender;

import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.entity.type.player.GeyserPlayerEntity;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.geyser.actor.GeyserCommandActor;
import revxrsal.commands.process.SenderResolver;

public enum GeyserSenderResolver implements SenderResolver<GeyserCommandActor> {

    INSTANCE;

    @Override public boolean isSenderType(@NotNull CommandParameter parameter) {
        return CommandSource.class.isAssignableFrom(parameter.type()) || GeyserPlayerEntity.class.isAssignableFrom(parameter.type());
    }

    @Override
    public @NotNull Object getSender(@NotNull Class<?> customSenderType, @NotNull GeyserCommandActor actor, @NotNull ExecutableCommand<GeyserCommandActor> command) {
        if (GeyserPlayerEntity.class.isAssignableFrom(customSenderType)) {
            return actor.requirePlayer();
        }
        if (GeyserConnection.class.isAssignableFrom(customSenderType)) {
            return actor.requireConnection();
        }
        return actor.source();
    }
}
