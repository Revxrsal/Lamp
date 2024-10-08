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
package revxrsal.commands.node.parser;

import lombok.SneakyThrows;
import org.jetbrains.annotations.*;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandFunction;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.command.Potential;
import revxrsal.commands.exception.UnknownCommandException;
import revxrsal.commands.exception.context.ErrorContext;
import revxrsal.commands.node.CommandRegistry;
import revxrsal.commands.reflect.MethodCaller.BoundMethodCaller;
import revxrsal.commands.reflect.MethodCallerFactory;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;
import revxrsal.commands.util.CommandPaths;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

import static revxrsal.commands.util.Collections.copyList;
import static revxrsal.commands.util.Collections.unmodifiableIterator;
import static revxrsal.commands.util.Reflections.getAllMethods;

@ApiStatus.Internal
public final class BaseCommandRegistry<A extends CommandActor> implements CommandRegistry<A> {

    private final List<ExecutableCommand<A>> children;
    private final List<ExecutableCommand<A>> unmodifiableChildren;
    private final Lamp<A> lamp;

    public BaseCommandRegistry(Lamp<A> lamp, List<ExecutableCommand<A>> children) {
        this.children = children;
        this.lamp = lamp;
        unmodifiableChildren = Collections.unmodifiableList(children);
    }

    public BaseCommandRegistry(Lamp<A> lamp) {
        this(lamp, new ArrayList<>());
    }

    public List<ExecutableCommand<A>> register(@NotNull Class<?> containerClass, Object instance) {
        return register(containerClass, instance, null);
    }

    @NotNull
    @SneakyThrows
    public @Unmodifiable List<ExecutableCommand<A>> register(@NotNull Class<?> containerClass, Object instance, @Nullable List<String> orphanPaths) {
        injectDependencies(containerClass, instance);
        List<ExecutableCommand<A>> registered = new ArrayList<>();
        for (Method method : getAllMethods(containerClass, true)) {
            AnnotationList annotations = AnnotationList.create(method)
                    .replaceAnnotations(method, lamp.annotationReplacers());
            if (annotations.isEmpty())
                continue;

            /* Not a command method (i.e. does not contain any annotation that indicates a command) */
            if (!isCommandMethod(annotations))
                continue;

            if (orphanPaths != null && !annotations.isEmpty()) {
                if (orphanPaths.isEmpty())
                    throw new IllegalArgumentException("Cannot have an OrphanCommand with no paths (supplied from .path())");
                String[] values = orphanPaths.toArray(new String[0]);
                annotations = annotations.withAnnotations(false, new DynamicCommand(values));
            }

            BoundMethodCaller caller = MethodCallerFactory.defaultFactory()
                    .createFor(method)
                    .bindTo(instance);

            CommandFunction fn = CommandFunctionImpl.create(method, annotations, lamp, caller);
            for (String path : CommandPaths.parseCommandAnnotations(containerClass, fn)) {
                MutableStringStream stream = StringStream.createMutable(path);
                ExecutableCommand<A> target = TreeParser.parse(fn, lamp, stream);
                if (lamp.hooks().onCommandRegistered(target)) {
                    add(target);
                    registered.add(target);
                }
            }
        }
        return copyList(registered);
    }

    private boolean isCommandMethod(AnnotationList annotations) {
        return annotations.contains(Command.class) || annotations.contains(Subcommand.class)
                || annotations.contains(CommandPlaceholder.class);
    }

    private void injectDependencies(Class<?> commandClass, Object instance) {
        for (Field field : commandClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Dependency.class))
                continue;
            if (!field.isAccessible())
                field.setAccessible(true);
            Object dependency = lamp.dependency(field.getType());
            try {
                field.set(instance, dependency);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to inject dependency value into field " + field.getName(), e);
            }
        }
    }

    private void add(@NotNull ExecutableCommand<A> command) {
        children.add(command);
        Collections.sort(children);
    }

    @Override
    public void execute(@NotNull A actor, @NotNull ExecutableCommand<A> command, @NotNull MutableStringStream input) {
        Potential<A> potential = command.test(actor, input);
        if (potential.failed())
            potential.handleException();
        else
            potential.execute();
    }

    @Override public @NotNull Lamp<A> lamp() {
        return lamp;
    }

    @Override
    public void execute(@NotNull A actor, @NotNull StringStream input) {
        LinkedList<Potential<A>> conflicts = new LinkedList<>();
        LinkedList<Potential<A>> failed = new LinkedList<>();
        String firstWord = input.peekUnquotedString();
        for (ExecutableCommand<A> execution : children) {
            // an easy way to exclude irrelevant nodes
            if (!execution.firstNode().name().equalsIgnoreCase(firstWord))
                continue;

            MutableStringStream in = input.toMutableCopy();
            Potential<A> potential = execution.test(actor, in);

            if (conflicts.size() >= lamp.dispatcherSettings().maximumFailedAttempts())
                break;

            if (potential.successful()) {
                conflicts.add(potential);
            } else {
                failed.add(potential);
            }
        }
        if (conflicts.isEmpty()) {
            if (failed.isEmpty()) {
                lamp.handleException(new UnknownCommandException(firstWord), ErrorContext.unknownCommand(actor));
                return;
            }
            lamp.dispatcherSettings().failureHandler().handleFailedAttempts(actor, Collections.unmodifiableList(failed), input);
            return;
        }
        Collections.sort(conflicts);
        conflicts.getFirst().execute();
    }

    @Override public @NotNull @UnmodifiableView List<ExecutableCommand<A>> commands() {
        return unmodifiableChildren;
    }

    @Override public void unregister(@NotNull ExecutableCommand<A> execution) {
        children.remove(execution);
    }

    @Override public boolean any(@NotNull Predicate<@NotNull ExecutableCommand<A>> matches) {
        return revxrsal.commands.util.Collections.any(children, matches);
    }

    @Override
    public @NotNull List<ExecutableCommand<A>> filter(@NotNull Predicate<@NotNull ExecutableCommand<A>> filterPredicate) {
        return revxrsal.commands.util.Collections.filter(children, filterPredicate);
    }

    @Override public void unregisterIf(@NotNull Predicate<ExecutableCommand<A>> matches) {
        children.removeIf(matches);
    }

    @Override public @NotNull Iterator<ExecutableCommand<A>> iterator() {
        return unmodifiableIterator(children.iterator());
    }

    private static final class DynamicCommand implements Command {
        private final String[] value;

        private DynamicCommand(String[] value) {this.value = value;}

        @Override
        public Class<? extends Annotation> annotationType() {
            return Command.class;
        }

        @Override public String[] value() {return value;}

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            DynamicCommand that = (DynamicCommand) obj;
            return Objects.equals(this.value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash((Object) value);
        }

        @Override
        public String toString() {
            return "DynamicCommand[" +
                    "value=" + Arrays.toString(value) + ']';
        }

    }
}
