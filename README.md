# PluginBase-Lamp

PluginBase-Lamp is a fork of Lamp that has been adapted to my own library, PluginBase.

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

## Getting Started
Now, for the good part.

First, you'll need to add the JitPack repository, and then you're going to select which part of Lamp you're going to use.

If you for some reason want to use the whole project as dependency, you can use "com.github.Revxrsal" as the groupId (or group on Gradle), and only the name "Lamp" as the artifactId (or name on Gradle).
But if you chose to use only one module or two, you have to use "com.github.Revxrsal.Lamp" as the groupId and "[the module that you want]" as the artifactId. Bellow, there are examples about Maven and Gradle that will help you get started.

### Maven
<details>
  <summary>pom.xml</summary>

  ``` xml
  <repositories>
      <repository>
          <id>jitpack.io</id>
          <url>https://jitpack.io</url>
      </repository>
  </repositories>

  <dependencies>
      <!-- For the common module -->
      <dependency>
          <groupId>com.github.Revxrsal.Lamp</groupId>
          <artifactId>common</artifactId> 
          <version>[version]</version>
      </dependency>

      <!-- For the bukkit module -->
      <dependency>
          <groupId>com.github.Revxrsal.Lamp</groupId>
          <artifactId>bukkit</artifactId>
          <version>[version]</version>
      </dependency>  
  </dependencies>
  ```
 </details>

### Gradle
<details>
  <summary>build.gradle (Groovy)</summary>

```groovy
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    // For the common module
    implementation 'com.github.Revxrsal.Lamp:common:[version]'

    // For the bukkit module
    implementation 'com.github.Revxrsal.Lamp:bukkit:[version]'
}

compileJava { // Preserve parameter names in the bytecode
    options.compilerArgs += ["-parameters"]
    options.fork = true
    options.forkOptions.executable = "javac"
}

compileKotlin { // optional: if you're using Kotlin
    kotlinOptions.javaParameters = true
}
```
</details>


<details>
  <summary>build.gradle.kts (Kotlin DSL)</summary>

```kotlin
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    // For the common project
    implementation("com.github.Revxrsal.Lamp:common:[version]")

    // For the bukkit module
    implementation("com.github.Revxrsal.Lamp:bukkit:[verison]")
}

compileJava { // Preserve parameter names in the bytecode
    options.compilerArgs += ["-parameters"]
    options.fork = true
    options.forkOptions.executable = "javac"
}

compileKotlin { // optional: if you're using Kotlin
    kotlinOptions.javaParameters = true
}
```
</details>

## Documentation
- **Overview**: [Lamp/wiki](https://github.com/Revxrsal/Lamp/wiki)
- **Examples**: [wiki/examples](https://github.com/Revxrsal/Lamp/wiki/Building-commands)
- **Javadocs**: https://revxrsal.github.io/Lamp/