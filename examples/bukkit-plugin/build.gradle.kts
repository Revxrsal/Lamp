import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    id("java")
    kotlin("jvm")
//    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("com.gradleup.shadow") version "9.4.2"

    // Adds runServer and runMojangMappedServer tasks for testing
    id("xyz.jpenilla.run-paper") version "2.2.4"

    // Generates plugin.yml based on the Gradle config
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1"
}

group = "io.github.revxrsal"

repositories {
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://hub.spigotmc.org/nexus/content/groups/public/")
    maven(url = "https://libraries.minecraft.net")
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":brigadier"))
    implementation(project(":bukkit"))
    implementation(kotlin("stdlib-jdk8"))
    compileOnly("net.kyori:adventure-api:4.18.0")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
//    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
}

//java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

tasks.withType<JavaCompile> {
    // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

// Generate our plugin.yml
bukkitPluginYaml {
    main = "com.example.plugin.TestPlugin"
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    authors.add("Revxrsal")
}

tasks["build"].dependsOn(tasks.shadowJar)