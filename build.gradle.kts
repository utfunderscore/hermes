plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.readutf.hermes"
version = "1.6.5"

repositories {
    maven { url = uri("https://repo.readutf.org/releases") }
}

subprojects {
    version = rootProject.version
    repositories {
        maven {
            url = uri("https://repo.readutf.org/releases")
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

println("version: $version")

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
