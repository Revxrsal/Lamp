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
package revxrsal.commands.minestom.actor;

import net.kyori.adventure.text.ComponentLike;
import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.util.Objects;

/**
 * Default implementation of {@link ActorFactory}
 */
final class BasicActorFactory implements ActorFactory<MinestomCommandActor> {

    public static final ActorFactory<MinestomCommandActor> INSTANCE = new BasicActorFactory(
            MinestomCommandActor::sendRawMessage,
            MinestomCommandActor::sendRawError
    );
    private final MessageSender<MinestomCommandActor, ComponentLike> messageSender;
    private final MessageSender<MinestomCommandActor, ComponentLike> errorSender;

    /**
     *
     */
    BasicActorFactory(
            MessageSender<MinestomCommandActor, ComponentLike> messageSender,
            MessageSender<MinestomCommandActor, ComponentLike> errorSender
    ) {
        this.messageSender = messageSender;
        this.errorSender = errorSender;
    }

    @Override
    public @NotNull MinestomCommandActor create(@NotNull CommandSender sender, @NotNull Lamp<MinestomCommandActor> lamp) {
        return new BasicMinestomActor(sender, lamp, messageSender, errorSender);
    }

    public MessageSender<MinestomCommandActor, ComponentLike> messageSender() {return messageSender;}

    public MessageSender<MinestomCommandActor, ComponentLike> errorSender() {return errorSender;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BasicActorFactory) obj;
        return Objects.equals(this.messageSender, that.messageSender) &&
                Objects.equals(this.errorSender, that.errorSender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageSender, errorSender);
    }

    @Override
    public String toString() {
        return "BasicActorFactory[" +
                "messageSender=" + messageSender + ", " +
                "errorSender=" + errorSender + ']';
    }

}
