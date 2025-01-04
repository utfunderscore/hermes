plugins {
    kotlin("jvm") version "2.0.0"
    id("java-library")
}

dependencies {
    // Add kotlin test
    testImplementation(kotlin("test"))

    // logging
    api("io.github.oshai:kotlin-logging-jvm:5.1.0")

    // Reflection
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")

    testRuntimeOnly("org.apache.logging.log4j:log4j-api:2.14.1")
    testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")
}