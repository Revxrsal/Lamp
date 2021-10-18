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
package revxrsal.commands.velocity.core;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.brigadier.LampBrigadier;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.util.ClassMap;

final class DummyVelocityBrigadier implements LampBrigadier {

    private final VelocityHandler handler;
    private final ClassMap<ArgumentType<?>> argumentTypes = new ClassMap<>();

    public DummyVelocityBrigadier(VelocityHandler handler) {
        this.handler = handler;
    }

    @Override public @NotNull CommandActor wrapSource(@NotNull Object commandSource) {
        return new VelocityActor((CommandSource) commandSource, handler.getServer());
    }

    @Override public void register(@NotNull LiteralCommandNode<?> node) {
        throw new UnsupportedOperationException();
    }

    @Override public @NotNull ClassMap<ArgumentType<?>> getAdditionalArgumentTypes() {
        return argumentTypes;
    }
}
