plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.readutf.hermes"
version = "1.0-SNAPSHOT"

repositories {
    maven { url = uri("https://reposilite.readutf.org/releases") }
}

subprojects {
    repositories {
        maven {
            url = uri("https://reposilite.readutf.org/releases")
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
