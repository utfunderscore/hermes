plugins {
    kotlin("jvm") version "2.0.0"
    id("java-library")
}

group = "org.readutf.hermes"
version = "1.0-SNAPSHOT"

dependencies {
    // logging
    api("io.github.oshai:kotlin-logging-jvm:5.1.0")

    // Expressible
    api("org.panda-lang:expressible:1.3.6") // Core library
    api("org.panda-lang:expressible-kt:1.3.6") // Kotlin extensions

    // Reflection
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
