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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Length;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.InvalidStringSizeException;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.process.ParameterValidator;

/**
 * A parameter validator that checks for the {@link Length @Length} annotation.
 */
@ApiStatus.Internal
public enum LengthChecker implements ParameterValidator<CommandActor, String> {
    INSTANCE;

    @Override
    public void validate(@NotNull CommandActor actor, String value, @NotNull ParameterNode<CommandActor, String> parameter, @NotNull Lamp<CommandActor> lamp) {
        Length range = parameter.annotations().get(Length.class);
        if (range == null)
            return;
        if (value.length() > range.max() || value.length() < range.min())
            throw new InvalidStringSizeException(range.min(), range.max(), value);
    }
}
