package revxrsal.commands.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandParameter;

public final class LinkedArgumentStack extends LinkedList<String> implements ArgumentStack {

  public LinkedArgumentStack(@NotNull Collection<? extends String> c) {
    super(c);
  }

  public LinkedArgumentStack(@NotNull String... c) {
    Collections.addAll(this, c);
  }

  private final List<String> unmodifiableView = Collections.unmodifiableList(this);

  @Override
  public @NotNull String join(String delimiter) {
    return String.join(delimiter, this);
  }

  @Override
  public @NotNull String join(@NotNull String delimiter, int startIndex) {
    StringJoiner joiner = new StringJoiner(delimiter);
    for (int i = startIndex; i < size(); i++) {
      joiner.add(get(i));
    }
    return joiner.toString();
  }

  @Override
  public @NotNull String popForParameter(@NotNull CommandParameter parameter) {
    if (parameter.consumesAllString()) {
      String value = join(" ");
      clear();
      return value;
    }
    return pop();
  }

  @Override
  public @NotNull @UnmodifiableView List<String> asImmutableView() {
    return unmodifiableView;
  }

  @Override
  public @NotNull @Unmodifiable List<String> asImmutableCopy() {
    return Collections.unmodifiableList(new ArrayList<>(this));
  }
}
