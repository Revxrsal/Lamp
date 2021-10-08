package our.commands;

import revxrsal.commands.annotation.Command;
import revxrsal.commands.cli.ConsoleActor;
import revxrsal.commands.cli.ConsoleCommandHandler;
import revxrsal.commands.cli.core.CLIHandler;
import revxrsal.commands.core.BaseCommandHandler;
import revxrsal.commands.process.ResponseHandler;

public class TestCLI {

    public static void main(String[] args) {
        ConsoleCommandHandler commandHandler = ConsoleCommandHandler.create();
        commandHandler.setHelpWriter((command, actor) -> String.format("%s %s - %s", command.getPath().toRealString(), command.getUsage(), command.getDescription()));
        commandHandler.registerResponseHandler(String.class, ResponseHandler::reply);
        commandHandler.register(new TestCLI());
        commandHandler.pollInput();
    }

    @Command("test")
    public void test(ConsoleActor actor, int width, int length) {
        System.out.println("width: " + width + ", length: " + length);
    }

}
