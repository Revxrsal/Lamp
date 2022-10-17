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
package revxrsal.commands.core;

import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ParameterValidator;
import revxrsal.commands.util.Primitives;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Setter
@ApiStatus.Internal
public final class EitherParameter extends ForwardingCommandParameter {

    private final CommandParameter delegate;
    private final Type type;
    private final Class<?> rawType;
    private final List<ParameterValidator<Object>> validators;

    public EitherParameter(CommandParameter delegate, Type type) {
        this.delegate = delegate;
        this.type = type;
        rawType = Primitives.getRawType(type);
        suggestionProvider = ((BaseAutoCompleter) delegate.getCommandHandler().getAutoCompleter()).getProvider(this);
        validators = new ArrayList<>(((BaseCommandHandler) delegate.getCommandHandler()).validators.getFlexibleOrDefault(rawType, Collections.emptyList()));
    }

    private ParameterResolver<Object> resolver;
    private SuggestionProvider suggestionProvider;

    @Override public @NotNull CommandParameter delegate() {
        return delegate;
    }

    @Override @NotNull public Class<?> getType() {
        return rawType;
    }

    @Override @NotNull public Type getFullType() {
        return type;
    }

    @Override public @NotNull <T> ParameterResolver<T> getResolver() {
        return (ParameterResolver<T>) resolver;
    }

    @Override public @NotNull SuggestionProvider getSuggestionProvider() {
        return suggestionProvider;
    }

    @Override public @NotNull @Unmodifiable List<ParameterValidator<Object>> getValidators() {
        return validators;
    }

    public static Type[] getTypes(CommandParameter parameter) {
        Type type = parameter.getFullType();
        if (!(type instanceof ParameterizedType))
            throw new IllegalArgumentException("'Either' parameter does not specify types!");
        return ((ParameterizedType) type).getActualTypeArguments();
    }

    public static EitherParameter[] create(CommandParameter parameter) {
        Type[] types = getTypes(parameter);
        return new EitherParameter[]{
                new EitherParameter(parameter, types[0]),
                new EitherParameter(parameter, types[1])
        };
    }
}
