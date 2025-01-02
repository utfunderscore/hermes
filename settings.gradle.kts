pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Hermes"

include("Core")

// Networking Libraries
include("Netty")

// Packet Serialization Libraries
include("Kryo")
include("FastJson")
include("Jackson")
