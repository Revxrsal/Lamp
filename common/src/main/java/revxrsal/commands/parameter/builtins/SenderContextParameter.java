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
package revxrsal.commands.parameter.builtins;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ContextParameter;
import revxrsal.commands.process.SenderResolver;

import java.util.Objects;

import static revxrsal.commands.util.Preconditions.notNull;

public final class SenderContextParameter<A extends CommandActor, T> implements ContextParameter<A, T> {
    private final SenderResolver<A> resolver;

    public SenderContextParameter(
            SenderResolver<A> resolver
    ) {this.resolver = resolver;}

    @Override
    public T resolve(@NotNull CommandParameter parameter, @NotNull ExecutionContext<A> context) {
        A actor = context.actor();
        Object sender = resolver.getSender(parameter.type(), actor, context.command());
        notNull(sender, "SenderResolver#getSender()");
        //noinspection unchecked
        return (T) sender;
    }

    public SenderResolver<A> resolver() {return resolver;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        SenderContextParameter that = (SenderContextParameter) obj;
        return Objects.equals(this.resolver, that.resolver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolver);
    }

    @Override
    public String toString() {
        return "SenderContextParameter[" +
                "resolver=" + resolver + ']';
    }

}
