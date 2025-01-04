plugins {
    kotlin("jvm")
    id("java-library")
    id("maven-publish")
}

dependencies {
    testImplementation(kotlin("test"))
    api(project(":core"))
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")
}

