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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.autocomplete.SuggestionProviderFactory;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.EntitySelector;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.bukkit.exception.MalformedEntitySelectorException;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.process.ValueResolver;
import revxrsal.commands.process.ValueResolver.ValueResolverContext;
import revxrsal.commands.process.ValueResolverFactory;
import revxrsal.commands.util.Primitives;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static revxrsal.commands.util.Preconditions.notNull;

public enum EntitySelectorResolver implements ValueResolverFactory {
    INSTANCE;

    private boolean supportComplexSelectors;

    EntitySelectorResolver() {
        try {
            Bukkit.getServer().selectEntities(Bukkit.getConsoleSender(), "@a");
            supportComplexSelectors = true;
        } catch (Throwable t) {
            supportComplexSelectors = false;
        }
    }

    @Override public @Nullable ValueResolver<?> create(@NotNull CommandParameter parameter) {
        if (EntitySelector.class.isAssignableFrom(parameter.getType())) {
            Class<?> entityType = (Class<?>) Primitives.getInsideGeneric(parameter.getFullType(), Entity.class);
            if (Player.class.isAssignableFrom(entityType)) {
                return this::resolvePlayerSelector;
            }
            return context -> {
                String selector = context.pop();
                try {
                    BukkitCommandActor actor = context.actor();
                    List<Entity> c = new ArrayList<>(Bukkit.getServer().selectEntities(actor.getSender(), selector));
                    c.removeIf(obj -> !entityType.isInstance(obj));
                    return new EntitySelectorImpl<>(c);
                } catch (IllegalArgumentException e) {
                    throw new MalformedEntitySelectorException(context.actor(), selector, e.getCause().getMessage());
                } catch (NoSuchMethodError e) {
                    throw new CommandErrorException("Entity selectors on legacy versions are not supported yet!");
                }
            };
        }
        return null;
    }

    private EntitySelector<Player> resolvePlayerSelector(ValueResolverContext context) {
        String selector = context.pop().toLowerCase();
        try {
            BukkitCommandActor bActor = context.actor();

            List<Player> coll;
            if (supportComplexSelectors) {
                coll = Bukkit.getServer().selectEntities(bActor.getSender(), selector).stream()
                        .filter(c -> c instanceof Player).map(Player.class::cast).collect(Collectors.toList());
                return new EntitySelectorImpl<>(coll);
            }
            coll = new ArrayList<>();
            Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
            switch (selector) {
                case "@r":
                    coll.add(players[ThreadLocalRandom.current().nextInt(players.length)]);
                    return new EntitySelectorImpl<>(coll);
                case "@a": {
                    Collections.addAll(coll, players);
                    return new EntitySelectorImpl<>(coll);
                }
                case "@s":
                case "@p": {
                    coll.add(bActor.requirePlayer());
                    return new EntitySelectorImpl<>(coll);
                }
                default: {
                    Player player = Bukkit.getPlayer(selector);
                    if (player == null)
                        throw new InvalidPlayerException(context.parameter(), selector);
                    coll.add(player);
                    return new EntitySelectorImpl<>(coll);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new MalformedEntitySelectorException(context.actor(), selector, e.getCause().getMessage());
        }
    }

    private static class EntitySelectorImpl<E extends Entity> extends ForwardingList<E> implements EntitySelector<E> {

        private final List<E> entities;

        public EntitySelectorImpl(List<E> entities) {
            this.entities = entities;
        }

        @Override protected List<E> delegate() {
            return entities;
        }

        @Override
        public boolean containsExactly(@NotNull Entity... entities) {
            notNull(entities, "entities");
            if (entities.length != this.entities.size()) {
                return false;
            }
            Set<Entity> set = new HashSet<>(entities.length);
            Collections.addAll(set, entities);
            return set.containsAll(this.entities);
        }

    }

    public enum SelectorSuggestionFactory implements SuggestionProviderFactory {
        INSTANCE;

        private boolean supportComplexSelectors;

        SelectorSuggestionFactory() {
            try {
                Bukkit.getServer().selectEntities(Bukkit.getConsoleSender(), "@a");
                supportComplexSelectors = true;
            } catch (Throwable t) {
                supportComplexSelectors = false;
            }
        }

        @Override public @Nullable SuggestionProvider createSuggestionProvider(@NotNull CommandParameter parameter) {
            if (parameter.getType().isAssignableFrom(EntitySelector.class)) {
                Class<? extends Entity> type = BukkitHandler.getSelectedEntity(parameter.getFullType());
                if (Player.class.isAssignableFrom(type) && !supportComplexSelectors) {
                    return SuggestionProvider.of("@a", "@p", "@r", "@s").compose(
                            parameter.getCommandHandler().getAutoCompleter().getSuggestionProvider("players")
                    );
                }
                if (!EntitySelectorResolver.INSTANCE.supportComplexSelectors) {
                    return SuggestionProvider.EMPTY;
                }
            }
            return null;
        }
    }

    public boolean supportsComplexSelectors() {
        return supportComplexSelectors;
    }
}
