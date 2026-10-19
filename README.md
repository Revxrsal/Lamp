# Lamp

[![Discord](https://discord.com/api/guilds/939962855476846614/widget.png)](https://discord.gg/pEGGF785zp)
[![Maven Central](https://img.shields.io/maven-metadata/v/https/repo1.maven.org/maven2/io/github/revxrsal/lamp.common/maven-metadata.xml.svg?label=maven%20central&colorB=brightgreen)](https://search.maven.org/artifact/io.github.revxrsal/lamp.common)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build](https://github.com/Revxrsal/Lamp/actions/workflows/build.yml/badge.svg)](https://github.com/Revxrsal/Lamp/actions/workflows/build.yml)
[![CodeFactor](https://www.codefactor.io/repository/github/revxrsal/lamp/badge)](https://www.codefactor.io/repository/github/revxrsal/lamp)

[**Documentation**](https://foxhut.gitbook.io/lamp-docs)

Welcome to Lamp, the versatile and powerful command framework designed for modern Java and Kotlin applications. Whether
you're building plugins for Minecraft servers, integrating with Discord, or creating command-line tools, Lamp provides a
robust solution with a clean, idiomatic API.

## Core Features

- **Multi-Platform Support**: Easily integrate with various platforms including Bukkit, BungeeCord, Sponge, Velocity,
  Geyser, JDA, Minestom, and more.
- **Command Management**:
    - Define commands using simple annotations and manage them effortlessly.
    - Use spaces to define subcommands easily
    - Have multiple variants of the same command
    - Define parameters that come in the middle of the command
- **Advanced Parameter Handling**: Support for context-based parameters, custom parameter types, and multiple parameter
  variants.
- **Optional parameters**: Define optional parameters and default values easily with `@Optional` and `@Default`
- **Flags and switches**:
  - Define Unix-style switches (`--switch` and `-s`) and flags (`--value <value>` or `-v <value>`)
  - Use shorthand-syntax `-s -p -r` as `-spr` 
- **Response Handling**: Configure how command responses are processed with flexible `ResponseHandlers`.
- **Dependency Injection**: Inject dependencies directly into command classes using a simple builder pattern.
- **Context Resolving**: Use context resolvers to handle complex command input scenarios.
- **Command Permissions**: Use built-in platform permissions, or define your own permission implementation with custom annotations.
- **Modular Design with Visitors**: Extend Lamp’s functionality through modular visitors for dynamic feature additions.
- **Custom annotation support**: Lamp allows you to create custom annotations and customize their behaviors.
- **Fool-proof**: Lamp has been designed to reduce user error as much as possible. It uses idiomatic APIs, immutability,
  and builders, to ensure you never run into bugs. Combined with extensive compiler annotations that will help you catch
  any problems at compile-time

## Command Examples

```java
@Command("greet user")
@Description("Sends a greeting message")
public void greet(CommandActor actor, @Default("World") String name) {
    actor.reply("Hello, " + name + "!");
}
```

### Multiple variants of the same command

```java
@Command("teleport <target> here")
public void teleportHere(Player sender, EntitySelector<LivingEntity> target) {
    for (LivingEntity entity : target)
        entity.teleport(sender);
}

@Command("teleport")
public void teleport(Player sender, double x, double y, double z) {
    sender.teleport(new Location(sender.getWorld(), x, y, z));
}
```

### Easily create aliases of commands

```java
@Command({"gamemode creative", "gmc"})
public void creative(@Default("me") Player sender) {
    sender.setGameMode(GameMode.CREATIVE);
}

@Command({"gamemode adventure", "gma"})
public void adventure(@Default("me") Player sender) {
    sender.setGameMode(GameMode.ADVENTURE);
}
```

### Support for Kotlin's default arguments

```kotlin
@Command("world")
fun teleportToWorld(
    sender: Player,
    @Optional target: Player = sender,
    @Optional world: World = sender.world
) {
}
```

### Integration with Brigadier

![Brigadier integration](https://github.com/Revxrsal/lamp-docs/blob/main/.gitbook/assets/image%20(4).png?raw=true)

### Custom parameter types

```kt
@CommandPermission("quests.command")
@Command("quest")
class QuestCommands {

  @Dependency
  private lateinit var questManager: QuestManager

  @Subcommand("create")
  fun createQuest(sender: CommandSender, name: String, description: String) {
  }

  @Subcommand("<quest> delete")
  fun deleteQuest(sender: CommandSender, quest: Quest) {
  }

  @Subcommand("<quest> start")
  fun startQuest(sender: Player, quest: Quest) {
  }

  @Subcommand("clear")
  fun clearQuests(sender: CommandSender) {
  }
}
```

### Flags and switches

```java
@Command("ban")
public void ban(
        CommandActor actor,
        Player target,
        @Switch("silent") boolean silent,
        @Flag("reason") @Optional String reason
) {
    // /ban Notch --silent --reason "Griefing spawn"
    // /ban Notch -s
}
```

### Suggestions

```java
@Command("difficulty")
public void difficulty(CommandActor actor, @Suggest({"easy", "normal", "hard"}) String level) {
}

@Command("warp")
public void warp(Player sender, @SuggestWith(WarpSuggestions.class) String warp) {
}
```

### Built-in validation

```java
@Command("nick")
public void nick(Player sender, @Length(min = 3, max = 16) String nickname) {
}

@Command("give coins")
public void giveCoins(CommandActor actor, Player target, @Range(min = 1, max = 10_000) int amount) {
}

@Command("mode")
public void mode(CommandActor actor, @Values({"survival", "creative"}) String mode) {
}
```

### Cooldowns

```java
@Command("heal")
@Cooldown(value = 30, unit = TimeUnit.SECONDS)
public void heal(Player sender) {
    sender.setHealth(sender.getMaxHealth());
}
```

### Writing your own parameter type

```java
public final class QuestParameterType implements ParameterType<BukkitCommandActor, Quest> {

    private final QuestManager questManager;

    public QuestParameterType(QuestManager questManager) {
        this.questManager = questManager;
    }

    @Override
    public Quest parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> context) {
        String name = input.readString();
        Quest quest = questManager.getQuest(name);
        if (quest == null)
            throw new CommandErrorException("No such quest: " + name);
        return quest;
    }

    @Override
    public @NotNull SuggestionProvider<BukkitCommandActor> defaultSuggestions() {
        return context -> questManager.getQuestNames();
    }
}
```

```java
Lamp<BukkitCommandActor> lamp = BukkitLamp.builder(this)
        .parameterTypes(types -> types.addParameterType(Quest.class, new QuestParameterType(questManager)))
        .build();
```

### Return values as responses

```java
@Command("balance")
public String balance(Player sender) {
    return "Your balance is $" + economy.getBalance(sender);
}
```

```java
BukkitLamp.builder(this)
        .responseHandler(String.class, (response, context) -> context.actor().reply(response))
        .build();
```

### Custom error messages

Handle any exception with a plain method — the parameters decide when it runs:

```java
public class MyExceptionHandler extends BukkitExceptionHandler {

    @HandleException
    public void onCooldown(CooldownException e, BukkitCommandActor actor) {
        actor.error("Slow down! Try again in " + e.getTimeLeft(TimeUnit.SECONDS) + "s.");
    }

    @HandleException
    public void onNoPermission(NoPermissionException e, BukkitCommandActor actor) {
        actor.error("You can't do that.");
    }
}
```

```java
BukkitLamp.builder(this)
        .exceptionHandler(new MyExceptionHandler())
        .build();
```

### Generated help commands

```java
@Command("arena help")
public void help(BukkitCommandActor actor, Help.RelatedCommands<BukkitCommandActor> commands, @Default("1") int page) {
    for (ExecutableCommand<BukkitCommandActor> command : commands.paginate(page, 7))
        actor.reply("/" + command.usage() + " - " + command.description());
}
```

### Command conditions

```java
BukkitLamp.builder(this)
        .commandCondition(context -> {
            if (maintenanceMode && !context.actor().sender().isOp())
                throw new CommandErrorException("The server is in maintenance mode.");
        })
        .build();
```

### Orphan commands

Let server owners choose the command name (e.g. from a config file):

```java
public class ShopCommand implements OrphanCommand {

    @CommandPlaceholder
    public void open(Player sender) {
        shop.open(sender);
    }

    @Subcommand("reload")
    public void reload(CommandActor actor) {
    }
}
```

```java
lamp.register(Orphans.path(config.getString("shop-command")).handler(new ShopCommand()));
```

### Kotlin coroutines

`suspend` functions work out of the box:

```kotlin
@Command("profile")
suspend fun profile(actor: BukkitCommandActor, target: OfflinePlayer) {
    val profile = database.fetchProfile(target.uniqueId)
    actor.reply("${target.name} has ${profile.coins} coins")
}
```

### Command-line applications

```java
public static void main(String[] args) {
    Lamp<ConsoleActor> lamp = CLILamp.builder().build();
    lamp.register(new PingCommand());
    lamp.accept(CLIVisitors.pollStdin());
}
```

### Custom permission annotations
```java
@RequiresGroup("admin")
@Command("admin command")
public void adminCommand(BukkitCommandActor actor) {
    actor.reply("You are authorized to use this command!")
}
```

### Supports Discord slash commands

```java
@Command("ban")
@Description("Bans the given user")
@CommandPermission(Permission.BAN_MEMBERS)
public void ban(
        SlashCommandActor actor,
        Member target,
        @Range(min = 1) int days
) {
    actor.replyToInteraction("User **" + target.getEffectiveName() + "** has been banned!").queue();
}
```

![JDA slash commands](https://github.com/Revxrsal/lamp-docs/blob/main/.gitbook/assets/image%20(3).png?raw=true)

### Supports Geyser extensions

```java
@Command("ping")
@CommandPermission("myextension.ping")
public void ping(GeyserConnection sender, GeyserConnection target) {
    sender.sendMessage(target.name() + "'s ping is " + target.ping() + "ms");
}
```

Commands whose sender is a `GeyserConnection` or `GeyserPlayerEntity` are automatically marked as Bedrock-only.
Geyser only accepts commands while it is defining them, so create Lamp and register your commands
in `GeyserPreInitializeEvent`:

```java
@Subscribe
public void onPreInitialize(GeyserPreInitializeEvent event) {
    Lamp<GeyserCommandActor> lamp = GeyserLamp.builder(this).build();
    lamp.register(new MyCommands());
}
```

### Multi-platform support

Lamp supports the following platforms out of the box:

- Bukkit / Spigot / Paper
- Brigadier
- BungeeCord
- Velocity
- Sponge
- Minestom
- Fabric
- Geyser extensions (requires Java 21)
- JDA slash commands
- Command-line applications

## Getting Started

To get started with Lamp, follow the instructions in our [setup guide](https://foxhut.gitbook.io/lamp-docs). Whether
you're using Gradle or
Maven, integrating Lamp into your project is straightforward.

For more detailed documentation on each feature, please refer to our **[detailed book](https://foxhut.gitbook.io/lamp-docs)** that outlines all features and APIs in Lamp

Happy coding with Lamp! 🚀

## Sponsors

If Lamp has made your life significantly easier or you're feeling particularly generous, consider sponsoring the
project! It's a great way to support the many hours I've spent maintaining this library and keeps me motivated. Please
don't sponsor if you can't afford it.

[Donate with PayPal](https://www.paypal.me/Recxrsion)

Huge thanks to those who donated! 😄

*If I missed you, or you would like to remain anonymous, feel free to shoot me a DM on Discord)*

- Demeng ($50)
- creatorfromhell ($10)
- Azoraqua ($10)
- SuperMax_8 ($8)
- Bed-dev ($6)
