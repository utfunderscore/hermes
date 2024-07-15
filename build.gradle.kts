import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.readutf.hermes"
//version = "1.3.0-SNAPSHOT-" +
//        LocalDateTime.now().format(
//            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"),
//        )
version = "1.3.0"

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
