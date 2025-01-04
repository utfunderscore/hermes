plugins {
    kotlin("jvm") version "2.0.0"
    id("maven-publish")
}

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("com.esotericsoftware:kryo5:5.6.0")
    compileOnly(project(":core"))
}