plugins {
    id("java")
}

repositories {
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://hub.spigotmc.org/nexus/content/groups/public/")
    maven(url = "https://libraries.minecraft.net")
}

val testing: Configuration by configurations.creating
configurations.implementation.get().extendsFrom(testing)

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(testing.asFileTree.files.map { zipTree(it) })
}

dependencies {
    testing(project(":common"))
    compileOnly("org.spigotmc:spigot-api:1.13.2-R0.1-SNAPSHOT")
    compileOnly("com.mojang:brigadier:1.0.18")
}

tasks.withType<JavaCompile> { // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}
