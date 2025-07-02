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

rootProject.name = "hermes"

include("core")

// Networking Libraries
include("netty")

// Packet Serialization Libraries
include("kryo")
include("jackson")
