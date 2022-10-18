package revxrsal.commands.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.reflect.MethodCaller.BoundMethodCaller;
import revxrsal.commands.process.ResponseHandler;
import revxrsal.commands.util.Preconditions;

class CommandExecutable implements ExecutableCommand {

  // lazily populated by CommandParses.
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
  @SuppressWarnings("rawtypes") ResponseHandler responseHandler = CommandParser.VOID_HANDLER;
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

  public void parent(BaseCommandCategory cat) {
    parent = cat;
    if (hasAnnotation(Default.class) && cat != null) {
      cat.defaultAction = this;
    } else {
      if (cat != null) {
        cat.commands.put(path, this);
      }
    }
  }

  public void setPermission(@NotNull CommandPermission permission) {
    Preconditions.notNull(permission, "permission");
    this.permission = permission;
  }

  @Override
  public String toString() {
    return "ExecutableCommand{" +
        "path=" + path +
        ", name='" + name + '\'' +
        '}';
  }

  @Override
  public int compareTo(@NotNull ExecutableCommand o) {
    return path.compareTo(o.getPath());
  }
}
