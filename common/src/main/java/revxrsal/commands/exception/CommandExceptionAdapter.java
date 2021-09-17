package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link CommandExceptionHandler} that inlines all exceptions
 * into individual, overridable methods. This greatly simplifies the process
 * of handling exceptions.
 * <p>
 * To handle additional exceptions, override the {@link #handleUnknown(Throwable)} method,
 * which is invoked on any exception that is not inlined here.
 */
public abstract class CommandExceptionAdapter implements CommandExceptionHandler {

    protected void handleUnknown(@NotNull Throwable throwable) {}

    protected void missingArgument(@NotNull MissingArgumentException exception) {}

    protected void invalidEnumValue(@NotNull EnumNotFoundException exception) {}

    protected void invalidUUID(@NotNull InvalidUUIDException exception) {}

    protected void invalidNumber(@NotNull InvalidNumberException exception) {}

    protected void invalidURL(@NotNull InvalidURLException exception) {}

    protected void invalidBoolean(@NotNull InvalidBooleanException exception) {}

    protected void noPermission(@NotNull NoPermissionException exception) {}

    protected void commandInvocation(@NotNull CommandInvocationException exception) {}

    protected void tooManyArguments(@NotNull TooManyArgumentsException exception) {}

    protected void invalidCommand(@NotNull InvalidCommandException exception) {}

    protected void invalidSubcommand(@NotNull InvalidSubcommandException exception) {}

    protected void noSubcommandSpecified(@NotNull NoSubcommandSpecifiedException exception) {}

    protected void cooldown(@NotNull CooldownException exception) {}

    @Override
    public final void handleException(@NotNull Throwable throwable) {
        if (throwable instanceof MissingArgumentException) missingArgument((MissingArgumentException) throwable);
        else if (throwable instanceof SendMessageException) ((SendMessageException) throwable).send();
        else if (throwable instanceof CommandErrorException) ((CommandErrorException) throwable).send();
        else if (throwable instanceof EnumNotFoundException) invalidEnumValue((EnumNotFoundException) throwable);
        else if (throwable instanceof InvalidNumberException) invalidNumber((InvalidNumberException) throwable);
        else if (throwable instanceof InvalidUUIDException) invalidUUID((InvalidUUIDException) throwable);
        else if (throwable instanceof InvalidURLException) invalidURL((InvalidURLException) throwable);
        else if (throwable instanceof InvalidBooleanException) invalidBoolean((InvalidBooleanException) throwable);
        else if (throwable instanceof CooldownException) cooldown((CooldownException) throwable);
        else if (throwable instanceof TooManyArgumentsException)
            tooManyArguments((TooManyArgumentsException) throwable);
        else if (throwable instanceof NoPermissionException) noPermission((NoPermissionException) throwable);
        else if (throwable instanceof CommandInvocationException)
            commandInvocation((CommandInvocationException) throwable);
        else if (throwable instanceof InvalidCommandException) invalidCommand((InvalidCommandException) throwable);
        else if (throwable instanceof InvalidSubcommandException)
            invalidSubcommand((InvalidSubcommandException) throwable);
        else if (throwable instanceof NoSubcommandSpecifiedException)
            noSubcommandSpecified((NoSubcommandSpecifiedException) throwable);
        else handleUnknown(throwable);
    }
}
