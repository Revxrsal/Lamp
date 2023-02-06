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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.*;
import revxrsal.commands.core.reflect.MethodCaller.BoundMethodCaller;
import revxrsal.commands.process.ResponseHandler;
import revxrsal.commands.util.Preconditions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static revxrsal.commands.util.Preconditions.notNull;

class CommandExecutable implements ExecutableCommand {

    // lazily populated by CommandParser.
    CommandHandler handler;
    boolean permissionSet = false;
    int id;
    CommandPath path;
    String name, usage, description;
    Method method;
    AnnotationReader reader;
    boolean secret;
    BoundMethodCaller methodCaller;
    BaseCommandCategory parent;
    @SuppressWarnings("rawtypes")
    ResponseHandler responseHandler = CommandParser.VOID_HANDLER;
    private CommandPermission permission = CommandPermission.ALWAYS_TRUE;
    @Unmodifiable List<CommandParameter> parameters;
    @Unmodifiable Map<Integer, CommandParameter> resolveableParameters;

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @Range(from = 0, to = Long.MAX_VALUE) int getId() {
        return id;
    }

    @Override
    public @NotNull String getUsage() {
        return usage;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public @NotNull CommandPath getPath() {
        return path;
    }

    @Override
    public @Nullable CommandCategory getParent() {
        return parent;
    }

    @Override
    public @NotNull @Unmodifiable List<CommandParameter> getParameters() {
        return parameters;
    }

    @Override
    public @NotNull @Unmodifiable Map<Integer, CommandParameter> getValueParameters() {
        return resolveableParameters;
    }

    @Override
    public @NotNull CommandPermission getPermission() {
        return permission;
    }

    @Override
    public @NotNull CommandHandler getCommandHandler() {
        return handler;
    }

    @Override
    public @NotNull <T> ResponseHandler<T> getResponseHandler() {
        return responseHandler;
    }

    @Override
    public void execute(@NotNull CommandActor actor, @Nullable Collection<String> input) {
        Preconditions.notNull(actor, "actor");
        ArgumentStack arguments = ArgumentStack.copyExact(path.path);
        if (input != null)
            arguments.addAll(input);
        getCommandHandler().dispatch(actor, arguments);
    }

    @Override
    public void execute(@NotNull CommandActor actor, @Nullable String... input) {
        Preconditions.notNull(actor, "actor");
        ArgumentStack arguments = ArgumentStack.copyExact(path.path);
        if (input != null)
            Collections.addAll(arguments, input);
        getCommandHandler().dispatch(actor, arguments);
    }

    @Override
    public boolean isSecret() {
        return secret;
    }

    @Override
    public <A extends Annotation> A getAnnotation(@NotNull Class<A> annotation) {
        return reader.get(annotation);
    }

    @Override
    public boolean hasAnnotation(@NotNull Class<? extends Annotation> annotation) {
        return reader.contains(annotation);
    }

    public void parent(BaseCommandCategory cat, boolean isDefault) {
        parent = cat;
        if (cat != null) {
            if (isDefault) {
                if (cat.defaultAction != null && cat.defaultAction.method != method)
                    throw new IllegalArgumentException("Category '" + cat.getPath().toRealString() + "' has more than one default" +
                            " action! (" + cat.defaultAction.method.toGenericString() + " and " + method.toGenericString() + ")");
                cat.defaultAction = this;
            } else
                cat.commands.put(path, this);
        }
    }

    public void setPermission(@NotNull CommandPermission permission) {
        notNull(permission, "permission");
        this.permission = permission;
    }

    @Override public String toString() {
        return "ExecutableCommand{" +
                "path=" + path +
                ", name='" + name + '\'' +
                '}';
    }

    @Override public int compareTo(@NotNull ExecutableCommand o) {
        return path.compareTo(o.getPath());
    }
}
