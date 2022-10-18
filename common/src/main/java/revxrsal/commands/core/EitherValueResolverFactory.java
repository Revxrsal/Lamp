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

import java.lang.reflect.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.core.BaseCommandDispatcher.ValueContextR;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ValueResolver;
import revxrsal.commands.process.ValueResolverFactory;
import revxrsal.commands.util.Either;

enum EitherValueResolverFactory implements ValueResolverFactory {
  INSTANCE;

  @Override
  public @Nullable ValueResolver<?> create(@NotNull CommandParameter parameter) {
    Class<?> rawType = parameter.getType();
    if (!Either.class.isAssignableFrom(rawType)) {
      return null;
    }

    Type[] types = EitherParameter.getTypes(parameter);

    EitherParameter first = generate(parameter, types[0]);
    EitherParameter second = generate(parameter, types[1]);

    return context -> {
      ArgumentStack original = context.arguments().copy();
      try {
        return Either.first(first.getResolver().resolve(context));
      } catch (Throwable t) {
        ((ValueContextR) context).argumentStack = original;
        return Either.second(second.getResolver().resolve(context));
      }
    };
  }

  private static EitherParameter generate(CommandParameter parameter, Type type) {
    EitherParameter either = new EitherParameter(parameter, type);
    ParameterResolver<Object> resolver = ((BaseCommandHandler) parameter.getCommandHandler()).getResolver(
        either);
    if (resolver == null) {
      throw new IllegalStateException(
          "Unable to find a resolver for parameter type " + either.getType());
    }
    if (!resolver.mutatesArguments()) {
      throw new IllegalStateException(
          "Only value-based arguments are allowed in the Either type (found " + either.getType()
              + ")");
    }
    either.setResolver(resolver);
    return either;
  }
}
