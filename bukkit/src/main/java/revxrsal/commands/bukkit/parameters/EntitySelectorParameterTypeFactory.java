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
package revxrsal.commands.bukkit.parameters;

import com.google.common.collect.ForwardingList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.EntitySelector;
import revxrsal.commands.bukkit.exception.MalformedEntitySelectorException;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static revxrsal.commands.util.Classes.getFirstGeneric;
import static revxrsal.commands.util.Classes.getRawType;
import static revxrsal.commands.util.Preconditions.notNull;

public final class EntitySelectorParameterTypeFactory implements ParameterType.Factory<BukkitCommandActor> {

    @Override
    @SuppressWarnings({"unchecked"})
    public @Nullable <T> ParameterType<BukkitCommandActor, T> create(@NotNull Type parameterType, @NotNull AnnotationList annotations, @NotNull Lamp<BukkitCommandActor> lamp) {
        Class<?> rawType = getRawType(parameterType);
        if (rawType != EntitySelector.class)
            return null;
        Class<? extends Entity> entityClass = getRawType(getFirstGeneric(parameterType, Entity.class))
                .asSubclass(Entity.class);
        return (ParameterType<BukkitCommandActor, T>) new EntitySelectorParameterType(entityClass);
    }

    static final class EntitySelectorParameterType implements ParameterType<BukkitCommandActor, EntitySelector<?>> {

        private final Class<?> entityType;

        public EntitySelectorParameterType(Class<?> entityType) {
            this.entityType = entityType;
        }

        @Override
        public EntitySelector<?> parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> context) {
            String selector = input.readString();
            try {
                List<Entity> c = new ArrayList<>(Bukkit.getServer().selectEntities(context.actor().sender(), selector));
                c.removeIf(obj -> !entityType.isInstance(obj));
                return new SelectorList<>(c);
            } catch (IllegalArgumentException e) {
                throw new MalformedEntitySelectorException(selector, e.getCause().getMessage());
            } catch (NoSuchMethodError e) {
                throw new CommandErrorException("Entity selectors on legacy versions are not supported yet!");
            }
        }
    }

    static final class SelectorList<E extends Entity> extends ForwardingList<E> implements EntitySelector<E> {

        private final List<E> entities;

        public SelectorList(List<E> entities) {
            this.entities = notNull(entities, "entities list");
        }

        @Override protected List<E> delegate() {
            return entities;
        }
    }
}
