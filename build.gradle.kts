import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java")
    id("com.vanniktech.maven.publish") version "0.29.0"
}

group = "io.github.revxrsal"
version = "4.0.0-beta.7"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

subprojects {

    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")

    val isExample = project.path.startsWith(":example")


    if (!isExample)
        apply(plugin = "com.vanniktech.maven.publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    if (!isExample)
        mavenPublishing {
            coordinates(
                groupId = group as String,
                artifactId = "lamp.$name",
                version = version as String
            )
            pom {
                name.set("Lamp")
                description.set("A modern annotation-driven commands framework for Java")
                inceptionYear.set("2024")
                url.set("https://github.com/Revxrsal/Lamp/")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://mit-license.org/")
                        distribution.set("https://mit-license.org/")
                    }
                }
                developers {
                    developer {
                        id.set("revxrsal")
                        name.set("Revxrsal")
                        url.set("https://github.com/Revxrsal/")
                    }
                }
                scm {
                    url.set("https://github.com/Revxrsal/Lamp/")
                    connection.set("scm:git:git://github.com/Revxrsal/Lamp.git")
                    developerConnection.set("scm:git:ssh://git@github.com/Revxrsal/Lamp.git")
                }
            }

            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

            signAllPublications()
        }


    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.30")
        annotationProcessor("org.projectlombok:lombok:1.18.30")

        compileOnly("org.jetbrains:annotations:24.0.1")
    }
}
