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
package revxrsal.commands.bukkit.core;

import com.google.common.collect.ForwardingList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.EntitySelector;
import revxrsal.commands.bukkit.exception.MalformedEntitySelectorException;
import revxrsal.commands.process.ValueResolver;

import java.util.List;

enum EntitySelectorResolver implements ValueResolver<EntitySelector> {
    INSTANCE;

    @Override public EntitySelector resolve(@NotNull ValueResolverContext context) throws Throwable {
        String selector = context.pop();
        try {
            BukkitCommandActor actor = context.actor();
            List<Entity> c = Bukkit.getServer().selectEntities(actor.getSender(), selector);
            return new EntitySelectorImpl(c);
        } catch (IllegalArgumentException e) {
            throw new MalformedEntitySelectorException(context.actor(), selector, e.getCause().getMessage());
        }
    }

    private static class EntitySelectorImpl extends ForwardingList<Entity> implements EntitySelector {

        private final List<Entity> entities;

        public EntitySelectorImpl(List<Entity> entities) {
            this.entities = entities;
        }

        @Override protected List<Entity> delegate() {
            return entities;
        }
    }
}
