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
package revxrsal.commands.autocomplete;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;

import java.lang.reflect.Type;
import java.util.Objects;

import static revxrsal.commands.util.Classes.getRawType;
import static revxrsal.commands.util.Classes.wrap;

/**
 * A {@link SuggestionProvider.Factory} that returns the given provider if the
 * type matches the given one.
 * <p>
 * Create using {@link SuggestionProvider.Factory#forType(Class, SuggestionProvider)}
 * and {@link SuggestionProvider.Factory#forTypeAndSubclasses(Class, SuggestionProvider)}
 */
final class ClassSuggestionProviderFactory<A extends CommandActor> implements SuggestionProvider.Factory<A> {
    private final Class<?> type;
    private final SuggestionProvider<A> provider;
    private final boolean allowSubclasses;


    ClassSuggestionProviderFactory(Class<?> type, SuggestionProvider<A> provider, boolean allowSubclasses) {
        this.type = wrap(type);
        this.provider = provider;
        this.allowSubclasses = allowSubclasses;
    }

    @Override
    public @Nullable SuggestionProvider<A> create(@NotNull Type parameterType, @NotNull AnnotationList annotations, @NotNull Lamp<A> lamp) {
        Class<?> pType = wrap(getRawType(parameterType));
        if (allowSubclasses && this.type.isAssignableFrom(pType)) {
            return provider;
        }
        if (this.type == pType)
            return provider;
        return null;
    }

    public Class<?> type() {return type;}

    public SuggestionProvider<A> provider() {return provider;}

    public boolean allowSubclasses() {return allowSubclasses;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ClassSuggestionProviderFactory that = (ClassSuggestionProviderFactory) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.provider, that.provider) &&
                this.allowSubclasses == that.allowSubclasses;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, provider, allowSubclasses);
    }

    @Override
    public String toString() {
        return "ClassSuggestionProviderFactory[" +
                "type=" + type + ", " +
                "provider=" + provider + ", " +
                "allowSubclasses=" + allowSubclasses + ']';
    }

}
