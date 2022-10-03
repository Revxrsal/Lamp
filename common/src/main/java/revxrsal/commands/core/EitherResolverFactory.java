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
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ParameterValidator;
import revxrsal.commands.process.ValueResolver;
import revxrsal.commands.process.ValueResolver.ValueResolverContext;
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
        Type first = p[0];
        Type second = p[1];

        MockParameter firstParameter = new MockParameter(parameter, first, Primitives.getRawType(first));
        ParameterResolver<Object> firstResolver = ((BaseCommandHandler) parameter.getCommandHandler()).getResolver(firstParameter);
        if (firstResolver == null) {
            throw new IllegalStateException("Unable to find a resolver for parameter type " + firstParameter.getType());
        }
        firstParameter.resolver = firstResolver;

        MockParameter secondParameter = new MockParameter(parameter, second, Primitives.getRawType(second));
        ParameterResolver<Object> secondResolver = ((BaseCommandHandler) parameter.getCommandHandler()).getResolver(secondParameter);
        if (secondResolver == null) {
            throw new IllegalStateException("Unable to find a resolver for parameter type " + secondParameter.getType());
        }
        secondParameter.resolver = secondResolver;

        return context -> {
            try {
                Context c = new Context(context, context.arguments().copy());
                return Either.first(firstParameter.resolver.resolve(c));
            } catch (Throwable t) {
                return Either.second(secondParameter.resolver.resolve(context));
            }
        };
    }

    // This is extremely hacky. If any better way exists, it should be used.
    @RequiredArgsConstructor
    @Setter
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


    @RequiredArgsConstructor
    static class Context implements ValueResolverContext {

        private final ValueResolverContext delegate;
        private final ArgumentStack arguments;

        @Override public ArgumentStack arguments() {return arguments;}

        @Override @NotNull @Unmodifiable public List<String> input() {return delegate.input();}

        @Override public <A extends CommandActor> @NotNull A actor() {return delegate.actor();}

        @Override @NotNull public CommandParameter parameter() {return delegate.parameter();}

        @Override @NotNull public ExecutableCommand command() {return delegate.command();}

        @Override @NotNull public CommandHandler commandHandler() {return delegate.commandHandler();}

        @Override public <T> @NotNull T getResolvedArgument(@NotNull Class<T> type) {return delegate.getResolvedArgument(type);}

        @Override public <T> @NotNull T getResolvedParameter(@NotNull CommandParameter parameter) {return delegate.getResolvedParameter(parameter);}

        @Override public String popForParameter() {return delegate.popForParameter();}

        @Override public String pop() {return delegate.pop();}

        @Override public int popInt() {return delegate.popInt();}

        @Override public double popDouble() {return delegate.popDouble();}

        @Override public byte popByte() {return delegate.popByte();}

        @Override public short popShort() {return delegate.popShort();}

        @Override public float popFloat() {return delegate.popFloat();}

        @Override public long popLong() {return delegate.popLong();}
    }

}
