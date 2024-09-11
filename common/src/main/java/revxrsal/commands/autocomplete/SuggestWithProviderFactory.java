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
import revxrsal.commands.annotation.SuggestWith;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.util.InstanceCreator;

import java.lang.reflect.Type;

/**
 * A {@link SuggestionProvider.Factory} that creates suggestions based on
 * the {@link SuggestWith} annotation
 */
public final class SuggestWithProviderFactory implements SuggestionProvider.Factory<CommandActor> {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @Nullable SuggestionProvider<CommandActor> create(@NotNull Type parameterType, @NotNull AnnotationList annotations, @NotNull Lamp<CommandActor> lamp) {
        SuggestWith suggestWith = annotations.get(SuggestWith.class);
        if (suggestWith == null)
            return null;
        BaseSuggestionProvider type = InstanceCreator.create(suggestWith.value());
        if (type instanceof SuggestionProvider<?> pType) {
            return (SuggestionProvider<CommandActor>) pType;
        } else if (type instanceof SuggestionProvider.Factory<?> factory) {
            return factory.create(parameterType, annotations, (Lamp) lamp);
        } else {
            throw new IllegalArgumentException("Don't know how to create a SuggestionProvider from " + type);
        }
    }
}
