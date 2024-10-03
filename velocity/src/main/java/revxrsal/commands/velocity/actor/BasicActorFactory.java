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
package revxrsal.commands.velocity.actor;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.process.MessageSender;

import java.util.Objects;

/**
 * Default implementation of {@link ActorFactory}
 */
final class BasicActorFactory implements ActorFactory<VelocityCommandActor> {

    public static final ActorFactory<VelocityCommandActor> INSTANCE = new BasicActorFactory(
            VelocityCommandActor::sendRawMessage,
            VelocityCommandActor::sendRawError
    );
    private final MessageSender<VelocityCommandActor, ComponentLike> messageSender;
    private final MessageSender<VelocityCommandActor, ComponentLike> errorSender;

    /**
     *
     */
    BasicActorFactory(
            MessageSender<VelocityCommandActor, ComponentLike> messageSender,
            MessageSender<VelocityCommandActor, ComponentLike> errorSender
    ) {
        this.messageSender = messageSender;
        this.errorSender = errorSender;
    }

    @Override
    public @NotNull VelocityCommandActor create(@NotNull CommandSource sender, @NotNull Lamp<VelocityCommandActor> lamp) {
        return new BasicVelocityActor(sender, lamp, messageSender, errorSender);
    }

    public MessageSender<VelocityCommandActor, ComponentLike> messageSender() {return messageSender;}

    public MessageSender<VelocityCommandActor, ComponentLike> errorSender() {return errorSender;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BasicActorFactory that = (BasicActorFactory) obj;
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
