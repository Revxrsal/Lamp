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
package revxrsal.commands.parameter;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.ParseWith;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.help.Help;
import revxrsal.commands.help.Help.ChildrenCommands;
import revxrsal.commands.help.Help.RelatedCommands;
import revxrsal.commands.help.Help.SiblingCommands;
import revxrsal.commands.parameter.builtins.*;
import revxrsal.commands.parameter.primitives.*;
import revxrsal.commands.stream.StringStream;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * An immutable registry of {@link ParameterType ParameterTypes} and
 * {@link ParameterType.Factory ParameterType.Factories}.
 * <p>
 * This type should be constructed using {@link ParameterTypes#builder()}.
 *
 * @param <A> The actor type
 * @see ParameterType
 * @see ContextParameter
 * @see ParseWith
 */
public final class ParameterTypes<A extends CommandActor> {

    /**
     * Highest priority factories. These come even before the user's
     * parameter types, but work in very specific conditions only.
     */
    private static final List<ParameterFactory> HIGHEST_PRIORITY_FACTORIES = List.of(
            ParseWithParameterTypeFactory.INSTANCE
    );

    /**
     * A list of default factories. These are registered at the very last
     * so they do not override user-registered factories.
     * <p>
     * These also will not be included in {@link #toBuilder()}.
     */
    private static final List<ParameterFactory> DEFAULT_FACTORIES = List.of(
            ArrayParameterTypeFactory.INSTANCE,
            ListParameterTypeFactory.INSTANCE,
            SetParameterTypeFactory.INSTANCE,
            EnumParameterTypeFactory.INSTANCE,
            OptionalParameterTypeFactory.INSTANCE,
            ParameterType.Factory.forType(int.class, new IntParameterType()),
            ParameterType.Factory.forType(double.class, new DoubleParameterType()),
            ParameterType.Factory.forType(long.class, new LongParameterType()),
            ParameterType.Factory.forType(float.class, new FloatParameterType()),
            ParameterType.Factory.forType(byte.class, new ByteParameterType()),
            ParameterType.Factory.forType(short.class, new ShortParameterType()),
            ParameterType.Factory.forType(char.class, new CharParameterType()),
            ParameterType.Factory.forType(boolean.class, new BooleanParameterType()),
            ParameterType.Factory.forType(UUID.class, new UUIDParameterType()),
            ParameterType.Factory.forType(String.class, StringParameterType.single()),
            ContextParameter.Factory.forType(StringStream.class, (parameter, context) -> context.input()),
            ContextParameter.Factory.forType(ExecutableCommand.class, (parameter, context) -> context.command()),
            ContextParameter.Factory.forType(Lamp.class, (parameter, context) -> context.lamp()),
            ContextParameter.Factory.forTypeAndSubclasses(CommandActor.class, (parameter, context) -> context.actor()),
            ContextParameter.Factory.forType(RelatedCommands.class, (parameter, context) -> context.command().relatedCommands(context.actor())),
            ContextParameter.Factory.forType(SiblingCommands.class, (parameter, context) -> context.command().siblingCommands(context.actor())),
            ContextParameter.Factory.forType(ChildrenCommands.class, (parameter, context) -> context.command().childrenCommands(context.actor()))
    );

    private final List<ParameterFactory> factories;
    private final int lastIndex;

    private ParameterTypes(@NotNull Builder<A> builder) {
        List<ParameterFactory> factories = new ArrayList<>(builder.factories.size() + DEFAULT_FACTORIES.size());
        factories.addAll(HIGHEST_PRIORITY_FACTORIES);
        factories.addAll(builder.factories);
        factories.addAll(DEFAULT_FACTORIES);
        this.factories = factories;
        this.lastIndex = builder.lastIndex;
    }

    private static boolean consumesInput(@NotNull ParameterFactory factory) {
        return factory instanceof ParameterType.Factory<?>;
    }

    @SuppressWarnings("unchecked")
    private static <A extends CommandActor, T> @Nullable ParameterResolver<A, T> toParameterResolver(
            Type type,
            AnnotationList annotations,
            Lamp<A> lamp,
            ParameterFactory factory
    ) {
        if (factory instanceof ParameterType.Factory) {
            ParameterType<A, T> parameterType = ((ParameterType.Factory<A>) factory).create(type, annotations, lamp);
            if (parameterType != null)
                return ParameterResolver.parameterType(parameterType);
        } else if (factory instanceof ContextParameter.Factory) {
            ContextParameter<A, T> contextParameter = ((ContextParameter.Factory<A>) factory).create(type, annotations, lamp);
            if (contextParameter != null)
                return ParameterResolver.contextParameter(contextParameter);
        }
        return null;
    }

    /**
     * Creates a new {@link Builder} for {@link ParameterType}s.
     *
     * @param <A> The actor type
     * @return The newly created {@link Builder}.
     */
    @Contract(value = "-> new", pure = true)
    public static <A extends CommandActor> @NotNull Builder<A> builder() {
        return new Builder<>();
    }

    /**
     * Returns the first {@link ParameterType} that can parse the given type
     * and annotations. This method can be used to create more complex parameter
     * types for composite types (such as {@code T[]})
     * <p>
     * Note that this method will never return {@code null}. In cases where no
     * suitable factory is found, it will throw {@link IllegalArgumentException}.
     *
     * @param type        The type to create for
     * @param annotations The annotations to pass to factories
     * @param lamp        The {@link Lamp} instance to pass to factories
     * @return The parameter type
     * @throws IllegalStateException if no suitable parameter type was found
     */
    public @NotNull <T> ParameterResolver<A, T> resolver(
            Type type,
            AnnotationList annotations,
            Lamp<A> lamp
    ) {
        for (ParameterFactory factory : factories) {
            ParameterResolver<A, T> parameterType = toParameterResolver(type, annotations, lamp, factory);
            if (parameterType != null)
                return parameterType;
        }
        throw new IllegalArgumentException("Failed to find a parameter resolver for type " + type);
    }

    /**
     * Returns the first {@link ParameterType} that comes after the {@code skipPast}
     * factory. This is useful for adding behavior on top of existing factory.
     *
     * @param type        The type to create for
     * @param annotations The annotations to pass to factories
     * @param skipPast    The factory to skip past. In most cases, this will be the caller factory,
     *                    i.e. {@code this}.
     * @param lamp        The {@link Lamp} instance to pass to factories
     * @return The parameter type
     * @throws IllegalStateException if no suitable parameter type was found
     */
    public <T> ParameterResolver<A, T> findNextResolver(
            Type type,
            AnnotationList annotations,
            ParameterFactory skipPast,
            Lamp<A> lamp
    ) {
        int skipPastIndex = factories.indexOf(skipPast);
        if (skipPastIndex == -1) {
            throw new IllegalArgumentException("Don't know how to skip past unknown resolver factory: " + skipPastIndex + " (it isn't registered?)");
        }

        for (int i = skipPastIndex + 1, size = factories.size(); i < size; i++) {
            ParameterFactory factory = factories.get(i);
            if (consumesInput(skipPast) != consumesInput(factory))
                continue;
            ParameterResolver<A, T> parameterType = toParameterResolver(type, annotations, lamp, factory);
            if (parameterType != null)
                return parameterType;
        }
        throw new IllegalArgumentException("Failed to find the next resolver for type " + type + " with annotations " + annotations);
    }

    /**
     * Creates a {@link Builder} that contains the factories registered
     * in this registry.
     *
     * @return The new builder
     */
    @Contract(value = "-> new", pure = true)
    public @NotNull Builder<A> toBuilder() {
        Builder<A> result = new Builder<>();
        for (int i = HIGHEST_PRIORITY_FACTORIES.size(); i < lastIndex; i++) {
            result.addFactory(factories.get(i));
        }
        for (int i = lastIndex, limit = factories.size() - DEFAULT_FACTORIES.size(); i < limit; i++) {
            result.addFactoryLast(factories.get(i));
        }
        return result;
    }

    public static class Builder<A extends CommandActor> {

        private final List<ParameterFactory> factories = new ArrayList<>();
        private int lastIndex = 0;

        /**
         * Registers a {@link ParameterType} that matches a specific class. Note
         * that this will not include subclasses of such class.
         * <p>
         * If you would like to include subclasses, register the factory
         * created by {@link ParameterType.Factory#forTypeAndSubclasses(Class, ParameterType)}.
         *
         * @param parameterClass The type to register for
         * @param type           The parameter type
         * @return This builder instance
         */
        public <T> Builder<A> addParameterType(@NotNull Class<T> parameterClass, @NotNull ParameterType<? super A, T> type) {
            return addFactory(ParameterType.Factory.forType(parameterClass, type));
        }

        /**
         * Registers a {@link ParameterType} that matches a specific class. Note
         * that this will not include subclasses of such class.
         * <p>
         * If you would like to include subclasses, register the factory
         * created by {@link ParameterType.Factory#forTypeAndSubclasses(Class, ParameterType)}.
         * <p>
         * Note that this will be given lower priority over other factories.
         *
         * @param parameterClass The type to register for
         * @param parameterType  The type to use
         * @return This builder instance
         */
        public <T> Builder<A> addParameterTypeLast(@NotNull Class<T> parameterClass, @NotNull ParameterType<? super A, T> parameterType) {
            return addFactoryLast(ParameterType.Factory.forType(parameterClass, parameterType));
        }

        /**
         * Registers a {@link ParameterType.Factory} to this registry.
         *
         * @param factory The factory to register
         * @return This builder instance
         */
        public Builder<A> addParameterTypeFactory(@NotNull ParameterType.Factory<? super A> factory) {
            return addFactory(factory);
        }

        /**
         * Registers a {@link ParameterType.Factory} to this registry.
         * <p>
         * Note that this will be given lower priority over other factories.
         *
         * @param factory The factory to register
         * @return This builder instance
         */
        public Builder<A> addParameterTypeFactoryLast(@NotNull ParameterType.Factory<? super A> factory) {
            return addFactoryLast(factory);
        }

        /* Context resolvers */

        /**
         * Registers a {@link ParameterType} that matches a specific class. Note
         * that this will not include subclasses of such class.
         * <p>
         * If you would like to include subclasses, register the factory
         * created by {@link ParameterType.Factory#forTypeAndSubclasses(Class, ParameterType)}.
         *
         * @param parameterClass The type to register for
         * @param type           The parameter type
         * @return This builder instance
         */
        public <T> Builder<A> addContextParameter(@NotNull Class<T> parameterClass, @NotNull ContextParameter<? super A, T> type) {
            return addFactory(ContextParameter.Factory.forType(parameterClass, type));
        }

        /**
         * Registers a {@link ParameterType} that matches a specific class. Note
         * that this will not include subclasses of such class.
         * <p>
         * If you would like to include subclasses, register the factory
         * created by {@link ParameterType.Factory#forTypeAndSubclasses(Class, ParameterType)}.
         * <p>
         * Note that this will be given lower priority over other factories.
         *
         * @param contextParameter The context parameter type
         * @param parameterClass   The type to register for
         * @return This builder instance
         */
        public <T> Builder<A> addContextParameterLast(@NotNull Class<T> parameterClass, @NotNull ContextParameter<? super A, T> contextParameter) {
            return addFactoryLast(ContextParameter.Factory.forType(parameterClass, contextParameter));
        }

        /**
         * Registers a {@link ParameterType.Factory} to this registry.
         *
         * @param factory The factory to register
         * @return This builder instance
         */
        public Builder<A> addContextParameterFactory(@NotNull ContextParameter.Factory<? super A> factory) {
            return addFactory(factory);
        }

        /**
         * Registers a {@link ParameterType.Factory} to this registry.
         * <p>
         * Note that this will be given lower priority over other factories.
         *
         * @param factory The factory to register
         * @return This builder instance
         */
        public Builder<A> addContextParameterFactoryLast(@NotNull ContextParameter.Factory<? super A> factory) {
            return addFactoryLast(factory);
        }

        private Builder<A> addFactory(@NotNull ParameterFactory factory) {
            factories.add(lastIndex++, factory);
            return this;
        }

        private Builder<A> addFactoryLast(@NotNull ParameterFactory factory) {
            factories.add(factory);
            return this;
        }

        /**
         * Creates a new {@link ParameterTypes} instance of this builder
         *
         * @return The newly created {@link ParameterTypes} registry.
         */
        @Contract(pure = true, value = "-> new")
        public @NotNull ParameterTypes<A> build() {
            return new ParameterTypes<>(this);
        }

    }
}
