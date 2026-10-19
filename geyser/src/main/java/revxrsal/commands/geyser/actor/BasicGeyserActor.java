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
package revxrsal.commands.geyser.actor;

import org.geysermc.geyser.api.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.geyser.util.GeyserUtils;

import java.util.UUID;

record BasicGeyserActor(
        @NotNull CommandSource source,
        @NotNull Lamp<GeyserCommandActor> lamp
) implements GeyserCommandActor {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    @Override public @NotNull String name() {
        return source.name();
    }

    @Override public @NotNull UUID uniqueId() {
        UUID playerUuid = source.playerUuid();
        return playerUuid == null ? CONSOLE_UUID : playerUuid;
    }

    @Override public void sendRawMessage(@NotNull String message) {
        source.sendMessage(message);
    }

    @Override public void sendRawError(@NotNull String message) {
        source.sendMessage(GeyserUtils.RED + message);
    }
}
