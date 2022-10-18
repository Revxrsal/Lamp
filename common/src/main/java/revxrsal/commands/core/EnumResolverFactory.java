package revxrsal.commands.core;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.CaseSensitive;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.EnumNotFoundException;
import revxrsal.commands.process.ValueResolver;
import revxrsal.commands.process.ValueResolverFactory;

enum EnumResolverFactory implements ValueResolverFactory {

  INSTANCE;

  @Override
  public @Nullable ValueResolver<?> create(@NotNull CommandParameter parameter) {
    Class<?> type = parameter.getType();
    if (!type.isEnum()) {
      return null;
    }
    Class<? extends Enum> enumType = type.asSubclass(Enum.class);
    Map<String, Enum<?>> values = new HashMap<>();
    boolean caseSensitive = parameter.hasAnnotation(CaseSensitive.class);
    for (Enum<?> enumConstant : enumType.getEnumConstants()) {
      if (caseSensitive) {
        values.put(enumConstant.name(), enumConstant);
      } else {
        values.put(enumConstant.name().toLowerCase(), enumConstant);
      }
    }
    return (ValueResolver<Enum<?>>) context -> {
      String value = context.pop();
      Enum<?> v = values.get(caseSensitive ? value : value.toLowerCase());
      if (v == null) {
        throw new EnumNotFoundException(parameter, value);
      }
      return v;
    };
  }
}
