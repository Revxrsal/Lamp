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

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.util.BukkitVersion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * A utility to modify {@link CommandNode}s reflectively.
 */
final class BrigadierUtil {

    // CommandNode#children, CommandNode#literals, CommandNode#arguments fields
    private static final Field CHILDREN_FIELD;
    private static final Field LITERALS_FIELD;
    private static final Field ARGUMENTS_FIELD;

    // nms.CommandListenerWrapper#getBukkitSender method
    private static final Method GET_BUKKIT_SENDER_METHOD;

    // An array of the CommandNode fields above: [#children, #literals, #arguments]
    private static final Field[] CHILDREN_FIELDS;

    static {
        try {
            Class<?> commandListenerWrapper;
            try {
                if (BukkitVersion.supports(1, 16))
                    commandListenerWrapper = BukkitVersion.findNmsClass("commands.CommandListenerWrapper");
                else
                    commandListenerWrapper = BukkitVersion.findNmsClass("CommandListenerWrapper");
            } catch (Exception e) {
                commandListenerWrapper = Class.forName("net.minecraft.commands.CommandListenerWrapper");
            }

            CHILDREN_FIELD = CommandNode.class.getDeclaredField("children");
            LITERALS_FIELD = CommandNode.class.getDeclaredField("literals");
            ARGUMENTS_FIELD = CommandNode.class.getDeclaredField("arguments");

            CHILDREN_FIELDS = new Field[]{CHILDREN_FIELD, LITERALS_FIELD, ARGUMENTS_FIELD};
            for (Field field : CHILDREN_FIELDS) {
                field.setAccessible(true);
            }

            GET_BUKKIT_SENDER_METHOD = commandListenerWrapper.getDeclaredMethod("getBukkitSender");
            GET_BUKKIT_SENDER_METHOD.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void removeChild(RootCommandNode root, String name) {
        try {
            for (Field field : CHILDREN_FIELDS) {
                Map<String, ?> children = (Map<String, ?>) field.get(root);
                children.remove(name);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static <S> LiteralCommandNode<S> renameLiteralNode(LiteralCommandNode<S> node, String newLiteral) {
        LiteralCommandNode<S> clone = new LiteralCommandNode<>(newLiteral, node.getCommand(), node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork());
        for (CommandNode<S> child : node.getChildren()) {
            clone.addChild(child);
        }
        return clone;
    }

    /**
     * Returns the underlying {@link CommandSender} of the given dispatcher
     *
     * @param commandSource The brigadier's command source
     * @return The {@link CommandSender}
     */
    public static @NotNull CommandSender getBukkitSender(Object commandSource) {
        Objects.requireNonNull(commandSource, "commandSource");
        try {
            return (CommandSender) GET_BUKKIT_SENDER_METHOD.invoke(commandSource);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
