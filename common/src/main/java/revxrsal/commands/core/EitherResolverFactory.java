/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copysecond (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copysecond notice and this permission notice shall be included in all
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
package revxrsal.commands.core;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.*;
import revxrsal.commands.core.BaseCommandDispatcher.ValueContextR;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ParameterValidator;
import revxrsal.commands.process.ValueResolver;
import revxrsal.commands.process.ValueResolverFactory;
import revxrsal.commands.util.Either;
import revxrsal.commands.util.Primitives;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

enum EitherResolverFactory implements ValueResolverFactory {
    INSTANCE;

    // We use hacky ways to create this sort of functionality. If any better way exists, it should
    // be used.
    @Override public @Nullable ValueResolver<?> create(@NotNull CommandParameter parameter) {
        Class<?> rawType = parameter.getType();
        if (!Either.class.isAssignableFrom(rawType)) return null;
        Type type = parameter.getFullType();
        if (!(type instanceof ParameterizedType))
            throw new IllegalArgumentException("'Either' parameter does not specify types!");
        Type[] p = ((ParameterizedType) type).getActualTypeArguments();
        Type firstType = p[0];
        Type secondType = p[1];

        MockParameter first = new MockParameter(parameter, firstType, Primitives.getRawType(firstType));
        ParameterResolver<Object> firstResolver = ((BaseCommandHandler) parameter.getCommandHandler()).getResolver(first);
        checkValid(first, firstResolver);
        first.resolver = firstResolver;

        MockParameter second = new MockParameter(parameter, secondType, Primitives.getRawType(secondType));
        ParameterResolver<Object> secondResolver = ((BaseCommandHandler) parameter.getCommandHandler()).getResolver(second);
        checkValid(second, secondResolver);
        second.resolver = secondResolver;

        return context -> {
            ArgumentStack original = context.arguments().copy();
            try {
                return Either.first(first.resolver.resolve(context));
            } catch (Throwable t) {
                ((ValueContextR) context).argumentStack = original;
                System.out.println("Testing for the 2nd argument type");
                System.out.println(context.arguments());
                return Either.second(second.resolver.resolve(context));
            }
        };
    }

    private static void checkValid(CommandParameter parameter, ParameterResolver<Object> resolver) {
        if (resolver == null) {
            throw new IllegalStateException("Unable to find a resolver for parameter type " + parameter.getType());
        }
        if (!resolver.mutatesArguments()) {
            throw new IllegalStateException("Only value-based arguments are allowed in the Either type (found " + parameter.getType() + ")");
        }
    }

    // This is extremely hacky. If any better way exists, it should be used.
    @Setter
    @RequiredArgsConstructor
    static class MockParameter implements CommandParameter {

        private final CommandParameter delegate;
        private final Type type;
        private final Class<?> rawType;
        private ParameterResolver<Object> resolver;

        @Override @NotNull public String getName() {return delegate.getName();}

        @Override @Nullable public String getDescription() {return delegate.getDescription();}

        @Override public int getMethodIndex() {return delegate.getMethodIndex();}

        @Override public int getCommandIndex() {return delegate.getCommandIndex();}

        @Override @NotNull public Class<?> getType() {return rawType;}

        @Override @NotNull public Type getFullType() {return type;}

        @Override public @NotNull @Unmodifiable List<String> getDefaultValue() {return delegate.getDefaultValue();}

        @Override public boolean consumesAllString() {return delegate.consumesAllString();}

        @Override public Parameter getJavaParameter() {return delegate.getJavaParameter();}

        @Override public @NotNull SuggestionProvider getSuggestionProvider() {return delegate.getSuggestionProvider();}

        @Override public @NotNull @Unmodifiable List<ParameterValidator<Object>> getValidators() {return delegate.getValidators();}

        @Override public boolean isOptional() {return delegate.isOptional();}

        @Override public boolean isLastInMethod() {return delegate.isLastInMethod();}

        @Override public boolean isSwitch() {return delegate.isSwitch();}

        @Override @NotNull public String getSwitchName() {return delegate.getSwitchName();}

        @Override public boolean isFlag() {return delegate.isFlag();}

        @Override @NotNull public String getFlagName() {return delegate.getFlagName();}

        @Override public boolean getDefaultSwitch() {return delegate.getDefaultSwitch();}

        @Override public @NotNull <T> ParameterResolver<T> getResolver() {return (ParameterResolver<T>) resolver;}

        @Override public @NotNull CommandHandler getCommandHandler() {return delegate.getCommandHandler();}

        @Override public @NotNull ExecutableCommand getDeclaringCommand() {return delegate.getDeclaringCommand();}

        @Override public @NotNull CommandPermission getPermission() {return delegate.getPermission();}

        @Override public int compareTo(@NotNull CommandParameter o) {return delegate.compareTo(o);}

        @Override public <A extends Annotation> A getAnnotation(@NotNull Class<A> annotation) {
            return delegate.getAnnotation(annotation);
        }

        @Override public boolean hasAnnotation(@NotNull Class<? extends Annotation> annotation) {
            return delegate.hasAnnotation(annotation);
        }

        @Override public boolean hasPermission(@NotNull CommandActor actor) {return delegate.hasPermission(actor);}

        @Override public void checkPermission(@NotNull CommandActor actor) {delegate.checkPermission(actor);}
    }
}
