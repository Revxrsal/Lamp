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

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Flag;
import revxrsal.commands.annotation.NotSender;
import revxrsal.commands.annotation.NotSender.ImpliesNotSender;
import revxrsal.commands.annotation.Single;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandFunction;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.CommandNode;
import revxrsal.commands.parameter.ContextParameter;
import revxrsal.commands.parameter.ParameterResolver;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.StringParameterType;
import revxrsal.commands.parameter.builtins.SenderContextParameter;
import revxrsal.commands.process.SenderResolver;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;
import revxrsal.commands.stream.token.LiteralToken;
import revxrsal.commands.stream.token.ParameterToken;
import revxrsal.commands.stream.token.Token;
import revxrsal.commands.stream.token.TokenParser;

import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class TreeParser<A extends CommandActor> {

    private final LinkedList<MutableCommandNode<A>> nodes = new LinkedList<>();
    private final @NotNull CommandFunction fn;
    private final Lamp<A> lamp;
    private final Map<String, CommandParameter> methodParameters;
    private final Set<String> usedLongNames = new HashSet<>();
    private final Set<Character> usedShortNames = new HashSet<>();
    private boolean requireOptionals;
    private boolean requireFlags;

    private TreeParser(@NotNull CommandFunction fn, @NotNull Lamp<A> lamp) {
        this.fn = fn;
        this.methodParameters = new LinkedHashMap<>(fn.parametersByName());
        this.lamp = lamp;
    }

    public static <A extends CommandActor> @NotNull ExecutableCommand<A> parse(
            @NotNull CommandFunction function,
            @NotNull Lamp<A> lamp,
            @NotNull MutableStringStream input
    ) {
        TreeParser<A> parser = new TreeParser<>(function, lamp);
        return parser.parse(input);
    }

    private Execution<A> parse(@NotNull MutableStringStream input) {
        ReflectionAction<A> action = new ReflectionAction<>(fn);
        checkNotEmpty(input);
        while (input.hasRemaining()) {
            if (input.peek() == ' ')
                input.moveForward();
            Token firstToken = TokenParser.parseNextToken(input);

            MutableCommandNode<A> node = generateNode(firstToken);
            if (isOptional(node)) {
                node.setAction(action);
            }

            checkOrder(node, input);
            pushNode(node);
        }
        if (!methodParameters.isEmpty()) {
            List<CommandParameter> arguments = new ArrayList<>();
            for (CommandParameter param : methodParameters.values()) {
                if (addSenderIfFirst(param, action))
                    continue;
                ParameterResolver<A, Object> resolver = lamp.resolver(param);
                if (resolver.isParameterType()) {
                    arguments.add(param);
                } else if (resolver.isContextParameter()) {
                    action.addContextParameter(param, resolver.requireContextParameter());
                }
            }
            for (CommandParameter argument : arguments) {
                MutableParameterNode<A, Object> node = createParameterNode(argument);
                if (isOptional(node))
                    node.setAction(action);
                checkOrder(node, input);
                pushNode(node);
            }
        }
        MutableCommandNode<A> last = nodes.getLast();
        last.setLast(true);
        last.setAction(action);
        if (isParameter(last))
            setIfGreedy(p(last));
        List<CommandNode<A>> executionNodes = new ArrayList<>();
        for (MutableCommandNode<A> node : nodes) {
            if (isParameter(node) && p(node).type().isGreedy() && !node.isLast()) {
                throw new IllegalArgumentException("Found a greedy parameter (" + node.getName() + ") in the middle of the command. " +
                        "Greedy parameters can only come at the end of the command.");
            }
            executionNodes.add(node.toNode());
        }
        Execution<A> executableCommand = new Execution<>(fn, executionNodes);
        executableCommand.forEach(node -> {
            ((BaseCommandNode) node).setCommand(executableCommand);
            ((BaseCommandNode) node).setLamp(lamp);
        });
        return executableCommand;
    }

    private boolean addSenderIfFirst(CommandParameter param, ReflectionAction<A> action) {
        if (param.methodIndex() == 0
                && !param.hasAnnotation(NotSender.class)
                && !param.annotations().any(a -> a.annotationType().isAnnotationPresent(ImpliesNotSender.class))
        ) {
            for (SenderResolver<? super A> senderResolver : lamp.senderResolvers()) {
                if (senderResolver.isSenderType(param)) {
                    SenderContextParameter<? super A, Object> resolver = new SenderContextParameter<>(senderResolver);
                    action.addContextParameter(param, (ContextParameter<A, ?>) resolver);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isGreedy(MutableParameterNode<A, Object> argument) {
        return argument.isLast()
                && !argument.parameter().annotations().contains(Single.class);
    }

    private void checkOrder(MutableCommandNode<A> node, @NotNull MutableStringStream src) {
        if (requireOptionals) {
            if (isLiteral(node))
                throw new IllegalArgumentException(
                        "Found a literal path (" + node.getName() + ") sitting between optional parameters (full path: " + src.source() + "). " +
                                "Optional parameters must all come successively at the end of the command");
            if (isParameter(node) && !isOptional(node))
                throw new IllegalArgumentException(
                        "Found a non-optional parameter (" + node.getName() + ") sitting between optional parameters (full path: " + src.source() + "). " +
                                "Optional parameters must all come successively at the end of the command");
        }
        if (requireFlags) {
            if (isLiteral(node)) {
                throw new IllegalArgumentException(
                        "Found a literal path (" + node.getName() + ") sitting between flag/switch parameters (full path: " + src.source() + "). " +
                                "Flags and switches must all come successively at the end of the command");
            }
            if (isParameter(node) && !isFlagOrSwitch(node))
                throw new IllegalArgumentException(
                        "Found a non-flag parameter (" + node.getName() + ") sitting between flags/switches parameters (full path: " + src.source() + "). " +
                                "Flags and switches must all come successively at the end of the command");

        }
    }

    private void checkNotEmpty(StringStream input) {
        if (input.hasFinished()) {
            if (input.source().isEmpty())
                throw new IllegalStateException("The input is empty");
            else
                throw new IllegalStateException("The input has already been consumed. Called parse() twice?");
        }
    }

    private void pushNode(MutableCommandNode<A> node) {
        if (nodes.isEmpty() && !isLiteral(node))
            throw new IllegalArgumentException("First node must be a literal.");
        validateFlagName(node);
        nodes.addLast(node);
    }

    private void validateFlagName(MutableCommandNode<A> node) {
        if (!(node instanceof MutableParameterNode<?, ?>)) {
            return;
        }
        MutableParameterNode<?, ?> parameter = (MutableParameterNode<?, ?>) node;
        Switch switchAnn = parameter.parameter().getAnnotation(Switch.class);
        Flag flag = parameter.parameter().getAnnotation(Flag.class);
        if (flag != null)
            validate(node, flag.value(), flag.shorthand());
        if (switchAnn != null)
            validate(node, switchAnn.value(), switchAnn.shorthand());
    }

    private void validate(MutableCommandNode<A> node, String value, char shorthand) {
        String name = value.isEmpty() ? node.getName() : value;
        Character shortcut = shorthand == '\0' ? name.charAt(0) : shorthand;
        if (!usedLongNames.add(name)) {
            throw new IllegalArgumentException("Duplicate flag name: " + value);
        }
        if (!usedShortNames.add(shortcut)) {
            throw new IllegalArgumentException("Duplicate flag shorthand name: " + shortcut);
        }
    }

    private MutableCommandNode<A> generateNode(@NotNull Token token) {
        if (token instanceof LiteralToken) {
            return createLiteralNode((LiteralToken) token);
        } else if (token instanceof ParameterToken) {
            return createParameterNode((ParameterToken) token);
        } else {
            throw new IllegalArgumentException("Don't know how to deal with token: " + token);
        }
    }

    private @NotNull MutableLiteralNode<A> createLiteralNode(@NotNull LiteralToken token) {
        return new MutableLiteralNode<>(token.value());
    }

    /* these methods help us reduce the code and casting we have to write. thanks, Java. */

    private boolean isParameter(@NotNull MutableCommandNode<A> node) {
        return node instanceof MutableParameterNode;
    }

    private boolean isLiteral(@NotNull MutableCommandNode<A> node) {
        return node instanceof MutableLiteralNode;
    }

    private boolean isOptional(MutableCommandNode<A> node) {
        return node instanceof MutableParameterNode && ((MutableParameterNode) node).isOptional();
    }

    private boolean isFlagOrSwitch(MutableCommandNode<A> node) {
        return node instanceof MutableParameterNode && (
                ((MutableParameterNode) node).parameter().hasAnnotation(Switch.class) || ((MutableParameterNode) node).parameter().hasAnnotation(Flag.class)
        );
    }

    private MutableParameterNode<A, Object> p(@NotNull MutableCommandNode<A> node) {
        return ((MutableParameterNode<A, Object>) node);
    }

    /**/

    private @NotNull CommandParameter popParameter(@NotNull String name) {
        CommandParameter parameter = methodParameters.remove(name);
        if (parameter == null) {
            throw new IllegalArgumentException("Couldn't find a parameter in method " + fn.method()
                    + " named '" + name + "'. Available names: " + methodParameters.keySet() + ".");
        }
        return parameter;
    }

    private MutableParameterNode<A, Object> createParameterNode(ParameterToken token) {
        CommandParameter parameter = popParameter(token.name());
        return createParameterNode(parameter);
    }

    private MutableParameterNode<A, Object> createParameterNode(CommandParameter parameter) {
        ParameterType<A, Object> parameterType = lamp
                .resolver(parameter)
                .requireParameterType(parameter.fullType());

        MutableParameterNode<A, Object> argument = new MutableParameterNode<>(parameter.name());
        argument.setType(parameterType);
        argument.setParameter(parameter);
        if (parameter.isOptional()) {
            argument.setOptional(true);
            requireOptionals = true;
        }
        if (parameter.hasAnnotation(Flag.class) || parameter.hasAnnotation(Switch.class)) {
            requireFlags = true;
        }
        argument.setPermission(lamp.createPermission(parameter.annotations()));
        setSuggestions(argument);
        return argument;
    }

    private void setSuggestions(MutableParameterNode<A, Object> argument) {
        SuggestionProvider<A> provider = lamp.suggestionProvider(argument.parameter());
        if (provider != SuggestionProvider.empty())
            argument.setSuggestions(provider);
    }

    private void setIfGreedy(@NotNull MutableParameterNode<A, Object> argument) {
        if (isGreedy(argument) && argument.type().equals(StringParameterType.single()) && !argument.parameter().hasAnnotation(Flag.class))
            argument.setType(((ParameterType) StringParameterType.greedy()));
    }

}
