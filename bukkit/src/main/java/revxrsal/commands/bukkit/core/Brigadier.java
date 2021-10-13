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
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.MinecraftArgumentTypes;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.EntitySelector;
import revxrsal.commands.bukkit.PlayerSelector;
import revxrsal.commands.bukkit.exception.MalformedEntitySelectorException;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.process.ValueResolver;
import revxrsal.commands.util.ClassMap;
import revxrsal.commands.util.Primitives;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static org.bukkit.NamespacedKey.minecraft;
import static revxrsal.commands.util.Preconditions.notNull;

final class Brigadier {

    private static final Class<?>[] ENTITIES = new Class<?>[]{boolean.class, boolean.class};

    private final Commodore commodore;
    private final ClassMap<ArgumentType<?>> argumentTypes = new ClassMap<>();

    public Brigadier(BukkitCommandHandler handler, Commodore commodore) {
        notNull(handler, "handler");
        this.commodore = notNull(commodore, "commodore");
        try {
            // 1st: single
            // 2nd: playersOnly
            argumentTypes.add(PlayerSelector.class, constructMinecraftArgumentType(minecraft("entity"), ENTITIES, false, true));
            argumentTypes.add(Player.class, constructMinecraftArgumentType(minecraft("entity"), ENTITIES, true, true));
            argumentTypes.add(EntitySelector.class, constructMinecraftArgumentType(minecraft("entity"), ENTITIES, false, false));
        } catch (Throwable ignored) {}
    }

    public static ValueResolver<EntitySelector> selectorResolver() {
        return context -> {
            String selector = context.pop();
            try {
                List<Entity> c = Bukkit.getServer().selectEntities(context.actor().as(BukkitCommandActor.class).getSender(), selector);
                return new EntitySelectorImpl(c);
            } catch (IllegalArgumentException e) {
                throw new MalformedEntitySelectorException(context.actor(), selector, e.getCause().getMessage());
            }
        };
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

    public void parse(@NotNull CommandHandler handler) {
        List<LiteralArgumentBuilder<?>> nodes = new ArrayList<>();
        List<CommandCategory> roots = handler.getCategories().values().stream().filter(c -> c.getPath().size() == 1).collect(Collectors.toList());
        for (CommandCategory root : roots) {
            nodes.add(parse(literal(root.getName()), root));
        }
        nodes.forEach(commodore::register);
    }

    private static ArgumentType<?> constructMinecraftArgumentType(NamespacedKey key, Class<?>[] argTypes, Object... args) {
        try {
            final Constructor<? extends ArgumentType<?>> constructor = MinecraftArgumentTypes.getClassByKey(key).getDeclaredConstructor(argTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    private LiteralArgumentBuilder<?> parse(LiteralArgumentBuilder<?> parent, CommandCategory category) {
        for (CommandCategory child : category.getCategories().values()) {
            LiteralArgumentBuilder childLiteral = parse(literal(child.getName()), child);
            parent.then(childLiteral);
        }
        for (ExecutableCommand child : category.getCommands().values()) {
            LiteralArgumentBuilder childLiteral = parse(literal(child.getName()), child);
            parent.then(childLiteral);
        }
        return parent;
    }

    @SuppressWarnings("rawtypes")
    private LiteralArgumentBuilder<?> parse(LiteralArgumentBuilder<?> parent, ExecutableCommand command) {
        CommandNode<?> lastParameter = null;
        for (CommandParameter parameter : command.getValueParameters().values()) {

            CommandNode node = getBuilder(command, parameter, true).build();
            if (lastParameter == null) {
                parent.then(node);
            } else {
                lastParameter.addChild(node);
            }
            lastParameter = node;
        }
        return parent;
    }

    private ArgumentBuilder getBuilder(ExecutableCommand command, CommandParameter parameter, boolean respectFlag) {
        if (parameter.isSwitch()) {
            return literal(parameter.getCommandHandler().getSwitchPrefix() + parameter.getSwitchName())
                    .executes(a -> 0);
        }
        if (parameter.isFlag() && respectFlag) {
            return literal(parameter.getCommandHandler().getFlagPrefix() + parameter.getFlagName())
                    .then(getBuilder(command, parameter, false));
        }
        ArgumentType<?> argumentType = getArgumentType(parameter);

        RequiredArgumentBuilder argumentBuilder = argument(parameter.getName(), argumentType)
                .suggests(createSuggestionProvider(command, parameter));
        if (parameter.isOptional())
            argumentBuilder.executes(context -> 1);
        return argumentBuilder;
    }

    private ArgumentType<?> getArgumentType(@NotNull CommandParameter parameter) {
        Class<?> type = Primitives.wrap(parameter.getType());
        ArgumentType<?> registeredType = argumentTypes.getFlexible(type);
        if (registeredType != null)
            return registeredType;
        @Nullable Range range = parameter.getAnnotation(Range.class);
        if (type == String.class) {
            if (parameter.consumesAllString())
                return greedyString();
            return string();
        } else if (type == Integer.class) {
            if (range == null)
                return integer();
            return integer((int) range.min(), (int) range.max());
        } else if (type == Double.class) {
            if (range == null)
                return doubleArg();
            return doubleArg(range.min(), range.max());
        } else if (type == Float.class) {
            if (range == null)
                return floatArg();
            return floatArg((float) range.min(), (float) range.max());
        } else if (type == Long.class) {
            if (range == null)
                return longArg();
            return longArg((long) range.min(), (long) range.max());
        } else if (type == Boolean.class) {
            return bool();
        }
        return string();
    }

    private SuggestionProvider<Object> createSuggestionProvider(
            ExecutableCommand command,
            CommandParameter parameter
    ) {
        if (parameter.getSuggestionProvider() == revxrsal.commands.autocomplete.SuggestionProvider.EMPTY)
            return null;
        return (context, builder) -> {
            try {
                CommandSender sender = commodore.getBukkitSender(context.getSource());
                BukkitCommandActor actor = BukkitCommandActor.wrap(sender);
                Message tooltip = new LiteralMessage(parameter.getName());
                ArgumentStack args = ArgumentStack.forAutoCompletion(context.getInput().substring(1));
                parameter
                        .getSuggestionProvider()
                        .getSuggestions(args, actor, command)
                        .forEach(c -> builder.suggest(c, tooltip));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return builder.buildFuture();
        };
    }
}
