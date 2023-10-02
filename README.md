# Lamp

[![Discord](https://discord.com/api/guilds/939962855476846614/widget.png)](https://discord.gg/pEGGF785zp)
[![JitPack](https://jitpack.io/v/Revxrsal/Lamp.svg)](https://jitpack.io/#Revxrsal/Lamp)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build](https://github.com/Revxrsal/Lamp/actions/workflows/gradle.yml/badge.svg)](https://github.com/Revxrsal/Lamp/actions/workflows/gradle.yml)

## Background
<details>
  <summary>Click to expand</summary>
Building commands has always been a core concept in many applications, and, lots of times, a really boring and cumbersome one to pull off: Having to think of all the possible input from the user, all the mistakes they will make, validating input and then finally executing the actual command logic. 

We *aren't* supposed to mess our hands up with so much of this. We really shouldn't get ourselves dirty with the highly error-prone string manipulation, nor are we supposed to repeat 3 lines of code a thousand times. We also should not be forced to think of all the edge cases and possible output of the user side. Developers should focus on what's *important*, not what isn't.

Then after all that, we really should make sure our code is clean, maintainable, extendable and flexible.

Building upon this belief, Lamp was born.

Lamp has taken responsibility upon itself to take all the crap of the command creation process: parsing input, validating arguments, auto completions, tokenizing and redirection, and leaves you only to the important part of your job here: the actual command logic.

Through annotations, parameter resolvers, command conditions, permissions, argument validators, cooldowns, dependency injection, auto-completers, Lamp not only makes the command creation process much easier, it also becomes more fun, intuitive and less error prone.
</details>

## There are many commands frameworks out there, why should I use Lamp?
Glad you asked!
- **Lamp is small**: The overall size of Lamp does not exceed 300 KB. Built to be lightweight and dependency-less, Lamp is convenient to package and ship.
- **Lamp is extendable**: Lamp has been built thoroughly with this in mind. You can create custom annotations for commands, parameters, permissions and resolvers, with their very own functionality. This gives so much space for your own extendability, and also helps make sure the code you write is minimal.
- **Lamp is portable**: Created with a high-level command API and an extendable codebase, Lamp has been produced to provide first-class support to as many platforms as possible. As of now, Lamp supports the following platforms:
    - [Bukkit / Spigot](bukkit)
    - [BungeeCord](bungee)
    - [VelocityPowered](velocity)
    - [SpongePowered](sponge)
    - [Java Discord API (JDA)](jda)
    - [Mojang's Brigadier](brigadier)
    - [Command line interface (CLI)](cli)

   With the help of the built-in APIs for dispatching commands and auto-completions, it is possible to support almost any platform out-of-the-box.
- **Lamp is easy**: Despite all the powerful features and extendability, getting started with Lamp couldn't be easier. Simply create a command handler for your appropriate platform, then proceed with creating your command with the main **\@Command** and **\@Subcommand** annotations, and finally registering it with **CommandHandler#register()**.
- **Lamp is powerful**: Lamp allows you to leverage some of the command features which would be otherwise too burdensome to build:
   - **[@Switch parameters](common/src/main/java/revxrsal/commands/annotation/Switch.java)**
   - **[@Flag (named) parameters](common/src/main/java/revxrsal/commands/annotation/Flag.java)**
   - **[Simple dependency injection](common/src/main/java/revxrsal/commands/annotation/Dependency.java)**
   - **[Built-in localization API](common/src/main/java/revxrsal/commands/locales/Translator.java)**
   - **[A quote-aware argument parser](common/src/main/java/revxrsal/commands/command/ArgumentStack.java)**
   - **[Context resolver factories](common/src/main/java/revxrsal/commands/process/ContextResolverFactory.java)** and **[value resolver factories](common/src/main/java/revxrsal/commands/process/ValueResolverFactory.java)**
   - **[Simple and powerful auto completions API](common/src/main/java/revxrsal/commands/autocomplete/AutoCompleter.java)**
   - **[Built-in command cooldown handler](common/src/main/java/revxrsal/commands/annotation/Cooldown.java)**
   - **First-class Kotlin support**: Lamp provides top-tier support for Kotlin features, such as:
     - Default parameters (with `@Optional`)
     - Suspend functions
     - Auxiliary Kotlin extensions

## Getting Started

See available versions and modules below.

### Maven

**pom.xml**
  
  ``` xml
  <repositories>
      <repository>
          <id>jitpack.io</id>
          <url>https://jitpack.io</url>
      </repository>
  </repositories>

  <dependencies>
      <!-- Required for all platforms -->
      <dependency>
          <groupId>com.github.Revxrsal.Lamp</groupId>
          <artifactId>common</artifactId> 
          <version>[version]</version>
      </dependency>

      <!-- Add your specific platform module here -->
      <dependency>
          <groupId>com.github.Revxrsal.Lamp</groupId>
          <artifactId>[module]</artifactId>
          <version>[version]</version>
      </dependency>  
  </dependencies>
  ```

### Gradle

**build.gradle (Groovy)**

```groovy
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    // Required for all platforms
    implementation 'com.github.Revxrsal.Lamp:common:[version]'

    // Add your specific platform module here
    implementation 'com.github.Revxrsal.Lamp:[module]:[version]'
}

compileJava { // Preserve parameter names in the bytecode
    options.compilerArgs += ["-parameters"]
}

compileKotlin { // optional: if you're using Kotlin
    kotlinOptions.javaParameters = true
}
```

**build.gradle.kts (Kotlin DSL)**

```kotlin
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    // Required for all platforms
    implementation("com.github.Revxrsal.Lamp:common:[version]")

    // Add your specific platform module here
    implementation("com.github.Revxrsal.Lamp:[module]:[verison]")
}

tasks.withType<JavaCompile> { // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

tasks.withType<KotlinJvmCompile> { // optional: if you're using Kotlin
    compilerOptions {
        javaParameters = true
    }
}
```
</details>

**Latest stable version**

[![JitPack](https://jitpack.io/v/Revxrsal/Lamp.svg)](https://jitpack.io/#Revxrsal/Lamp)

**Available modules**:
- `bukkit` for Bukkit/Spigot/Paper
- `velocity` for [VelocityPowered](https://velocitypowered.com/)
- `sponge` for [SpongePowered](https://spongepowered.org/)
- `bungee` for [BungeeCord](https://www.spigotmc.org/wiki/bungeecord/)
- `jda` for [Java Discord API (JDA)](https://github.com/DV8FromTheWorld/JDA)
- `brigadier` for [Mojang Brigadier](https://github.com/Mojang/brigadier)
- `cli` for building console applications

## Examples
Creating a command handler
```java
public final class BansPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Create a command handler here
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.register(new BansCommand());
        // (Optional) Register colorful tooltips (Works on 1.13+ only) 
        handler.registerBrigadier();
    }
}
```

**`/epicbans ban <player> <days> <reason>`**

Add `-silent` to make the ban silent
```java
    @Command("epicbans ban")
    public void banPlayer(
            Player sender,
            @Range(min = 1) long days,
            Player toBan,
            String reason,
            @Switch("silent") boolean silent
    ) {
        if (!silent)
            Bukkit.broadcastMessage(colorize("Player &6" + toBan.getName() + " &fhas been banned!"));
        Date expires = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days));
        Bukkit.getBanList(Type.NAME).addBan(toBan.getName(), reason, expires, sender.getName());
    }
```

**Commands to switch game-modes**
```java
    @Command({"gmc", "gamemode creative"})
    public void creative(@Default("me") Player sender) {
        sender.setGameMode(GameMode.CREATIVE);
    }

    @Command({"gms", "gamemode survival"})
    public void survival(@Default("me") Player sender) {
        sender.setGameMode(GameMode.SURVIVAL);
    }

    @Command({"gma", "gamemode adventure"})
    public void adventure(@Default("me") Player sender) {
        sender.setGameMode(GameMode.ADVENTURE);
    }

    @Command({"gmsp", "gamemode spectator"})
    public void spectator(@Default("me") Player sender) {
        sender.setGameMode(GameMode.SPECTATOR);
    }
```

**Commands to ping online operators, with 10 minutes delay**

```java  
    @Command({"opassist", "opa", "helpop"})
    @Cooldown(value = 10, unit = TimeUnit.MINUTES)
    public void requestAssist(Player sender, String query) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                player.sendMessage(colorize("&a" + sender.getName() + " &fneeds help: &b" + query));
            }
        }
    }
```

**Terminate all nearby entities command**

```java
    @Command("terminate")
    public void terminate(BukkitCommandActor sender, @Range(min = 1) int radius) {
        int killCount = 0;
        for (Entity target : sender.requirePlayer().getNearbyEntities(radius, radius, radius)) {
            if (target instanceof LivingEntity) {
                ((LivingEntity) target).setHealth(0);
                killCount++;
            }
        }
        sender.reply("&aSuccessfully killed &e" + killCount +" &aplayers!");
    }
```

With Brigadier:

![Radius accepted as it is within range](https://i.imgur.com/VnmCiDy.png)

![Radius not accepted](https://i.imgur.com/3N4xW19.png)

**Message players with player selectors**

```java
    @Command("pm")
    public void message(Player sender, EntitySelector<Player> players, String message) {
        for (Player player : players) {
            player.sendMessage(sender.getName() + " -> You: " + ChatColor.GOLD + message);
        }
    }
```

![Example selector](https://i.imgur.com/JK0373h.png)

**More examples available [here](https://github.com/Revxrsal/Lamp/wiki/Building-commands)**

## Documentation
- **Overview**: [Lamp/wiki](https://github.com/Revxrsal/Lamp/wiki)
- **Examples**: [wiki/examples](https://github.com/Revxrsal/Lamp/wiki/Building-commands)
- **Javadocs**: https://revxrsal.github.io/Lamp/
