# Lamp

[![Discord](https://discord.com/api/guilds/939962855476846614/widget.png)](https://discord.gg/pEGGF785zp)
[![Maven Central](https://img.shields.io/maven-metadata/v/https/repo1.maven.org/maven2/io/github/revxrsal/lamp.common/maven-metadata.xml.svg?label=maven%20central&colorB=brightgreen)](https://search.maven.org/artifact/io.github.revxrsal/lamp.common)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build](https://github.com/Revxrsal/Lamp/actions/workflows/gradle.yml/badge.svg)](https://github.com/Revxrsal/Lamp/actions/workflows/gradle.yml)

# Lamp Library

Welcome to Lamp, the versatile and powerful command framework designed for modern Java and Kotlin applications. Whether
you're building plugins for Minecraft servers, integrating with Discord, or creating command-line tools, Lamp provides a
robust solution with a clean, idiomatic API.

## Core Features

- **Multi-Platform Support**: Easily integrate with various platforms including Bukkit, BungeeCord, Sponge, Velocity,
  JDA,
  and more.
- **Command Management**:
    - Define commands using simple annotations and manage them effortlessly.
    - Use spaces to define subcommands easily
    - Have multiple variants of the same command
    - Define parameters that come in the middle of the command
- **Advanced Parameter Handling**: Support for context-based parameters, custom parameter types, and multiple parameter
  variants.
- **Response Handling**: Configure how command responses are processed with flexible `ResponseHandlers`.
- **Dependency Injection**: Inject dependencies directly into command classes using a simple builder pattern.
- **Context Resolving**: Use context resolvers to handle complex command input scenarios.
- **Command Permissions**: Apply granular permissions with `@CommandPermission` annotations.
- **Modular Design with Visitors**: Extend Lamp’s functionality through modular visitors for dynamic feature additions.
- **Fool-proof**: Lamp has been designed to reduce user error as much as possible. It uses idiomatic APIs, immutability,
  and builders, to ensure you never run into bugs. Combined with extensive compiler annotations that will help you catch
  any problems at compile-time

## Command Examples

```java
@Command("greet user")
@Description("Sends a greeting message")
public void greet(CommandActor actor, @Optional("World") String name) {
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

### Custom parameter types and dependency injection

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

![JDA slahs commands](https://github.com/Revxrsal/lamp-docs/blob/main/.gitbook/assets/image%20(3).png?raw=true)

### Multi-platform support

Lamp supports the following platforms out of the box:

- Bukkit / Spigot / Paper
- Brigadier
- BungeeCord
- Velocity
- Sponge
- JDA slash commands
- Command-line applications

## Getting Started

To get started with Lamp, follow the instructions in our [setup guide](https://foxhut.gitbook.io/lamp-docs). Whether
you're using Gradle or
Maven, integrating Lamp into your project is straightforward.

For more detailed documentation on each feature, please refer to our *
*[detailed book](https://foxhut.gitbook.io/lamp-docs)** that outlines all features and APIs in Lamp

Happy coding with Lamp! 🚀

## Sponsors

If Lamp has made your life significantly easier or you're feeling particularly generous, consider sponsoring the
project! It's a great way to support the many hours I've spent maintaining this library and keeps me motivated. Please
don't sponsor if you can't afford it.

[Donate with PayPal](https://www.paypal.me/Recxrsion)

Huge thanks to those who donated! 😄

*If I missed you, or you would like to remain anonymous, feel free to shoot me a DM on Discord)*

- Demeng ($50)
