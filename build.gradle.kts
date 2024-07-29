plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.readutf.hermes"
version = "1.5.3"

repositories {
    maven { url = uri("https://reposilite.readutf.org/releases") }
}

subprojects {
    version = rootProject.version
    repositories {
        maven {
            url = uri("https://reposilite.readutf.org/releases")
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
