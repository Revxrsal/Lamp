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
package revxrsal.commands.bukkit.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;

import java.lang.reflect.Field;
import java.util.function.Predicate;

final class NodeReflection {

    private static final Field command, requirement, customSuggestions;

    static {
        try {
            command = CommandNode.class.getDeclaredField("command");
            command.setAccessible(true);
            requirement = CommandNode.class.getDeclaredField("requirement");
            requirement.setAccessible(true);
            customSuggestions = ArgumentCommandNode.class.getDeclaredField("customSuggestions");
            customSuggestions.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void setCommand(CommandNode<T> node, Command<T> command) {
        try {
            NodeReflection.command.set(node, command);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static <T> void setRequirement(CommandNode<T> node, Predicate<T> requirement) {
        try {
            NodeReflection.requirement.set(node, requirement);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static <S, T> void setSuggestionProvider(ArgumentCommandNode<S, T> node, SuggestionProvider<S> provider) {
        try {
            customSuggestions.set(node, provider);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
