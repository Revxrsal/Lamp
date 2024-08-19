plugins {
    id("java")
}

repositories {
    maven(url = "https://nexus.velocitypowered.com/repository/maven-public/")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":brigadier"))
    compileOnly("com.velocitypowered:velocity-api:3.0.0")
}
