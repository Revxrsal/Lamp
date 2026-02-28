plugins {
    id("java")
}

repositories {
    maven(url = "https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":brigadier"))
    compileOnly("com.velocitypowered:velocity-api:3.0.0")
}
