pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}

rootProject.name = "hermes"

include("core")

// Networking Libraries
include("netty")

// Packet Serialization Libraries
include("kryo")
include("jackson")

include("core")
include("neo")
include("kryo")