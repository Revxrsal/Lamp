package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandActor;

/**
 * An implementation of {@link CommandExceptionHandler} that inlines all exceptions
 * into individual, overridable methods. This greatly simplifies the process
 * of handling exceptions.
 * <p>
 * To handle additional exceptions, override the {@link #handleUnknown(CommandActor, Throwable)} method,
 * which is invoked on any exception that is not inlined here.
 */
public abstract class CommandExceptionAdapter implements CommandExceptionHandler {

    protected void handleUnknown(@NotNull CommandActor actor, @NotNull Throwable throwable) {}

    protected void missingArgument(@NotNull CommandActor actor, @NotNull MissingArgumentException exception) {}

    protected void invalidEnumValue(@NotNull CommandActor actor, @NotNull EnumNotFoundException exception) {}

    protected void invalidUUID(@NotNull CommandActor actor, @NotNull InvalidUUIDException exception) {}

    protected void invalidNumber(@NotNull CommandActor actor, @NotNull InvalidNumberException exception) {}

    protected void invalidURL(@NotNull CommandActor actor, @NotNull InvalidURLException exception) {}

    protected void invalidBoolean(@NotNull CommandActor actor, @NotNull InvalidBooleanException exception) {}

    protected void noPermission(@NotNull CommandActor actor, @NotNull NoPermissionException exception) {}

    protected void commandInvocation(@NotNull CommandActor actor, @NotNull CommandInvocationException exception) {}

    protected void tooManyArguments(@NotNull CommandActor actor, @NotNull TooManyArgumentsException exception) {}

    protected void invalidCommand(@NotNull CommandActor actor, @NotNull InvalidCommandException exception) {}

    protected void invalidSubcommand(@NotNull CommandActor actor, @NotNull InvalidSubcommandException exception) {}

    protected void noSubcommandSpecified(@NotNull CommandActor actor, @NotNull NoSubcommandSpecifiedException exception) {}

    protected void cooldown(@NotNull CommandActor actor, @NotNull CooldownException exception) {}

    @Override
    public final void handleException(@NotNull Throwable throwable, @NotNull CommandActor actor) {
        if (throwable instanceof MissingArgumentException) missingArgument(actor, (MissingArgumentException) throwable);
        else if (throwable instanceof SendMessageException) ((SendMessageException) throwable).sendTo(actor);
        else if (throwable instanceof CommandErrorException) ((CommandErrorException) throwable).sendTo(actor);
        else if (throwable instanceof EnumNotFoundException) invalidEnumValue(actor, (EnumNotFoundException) throwable);
        else if (throwable instanceof InvalidNumberException) invalidNumber(actor, (InvalidNumberException) throwable);
        else if (throwable instanceof InvalidUUIDException) invalidUUID(actor, (InvalidUUIDException) throwable);
        else if (throwable instanceof InvalidURLException) invalidURL(actor, (InvalidURLException) throwable);
        else if (throwable instanceof InvalidBooleanException)
            invalidBoolean(actor, (InvalidBooleanException) throwable);
        else if (throwable instanceof CooldownException) cooldown(actor, (CooldownException) throwable);
        else if (throwable instanceof TooManyArgumentsException)
            tooManyArguments(actor, (TooManyArgumentsException) throwable);
        else if (throwable instanceof NoPermissionException) noPermission(actor, (NoPermissionException) throwable);
        else if (throwable instanceof CommandInvocationException)
            commandInvocation(actor, (CommandInvocationException) throwable);
        else if (throwable instanceof InvalidCommandException)
            invalidCommand(actor, (InvalidCommandException) throwable);
        else if (throwable instanceof InvalidSubcommandException)
            invalidSubcommand(actor, (InvalidSubcommandException) throwable);
        else if (throwable instanceof NoSubcommandSpecifiedException)
            noSubcommandSpecified(actor, (NoSubcommandSpecifiedException) throwable);
        else handleUnknown(actor, throwable);
    }
}
