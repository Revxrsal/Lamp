plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    compileOnly("net.dv8tion:JDA:5.1.0")
}