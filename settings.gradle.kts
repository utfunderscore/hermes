plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "Hermes"
include("Core")
include("Kryo")
include("Netty")
include("FastJson")
