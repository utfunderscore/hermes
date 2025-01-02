plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("io.netty:netty-all:4.1.111.Final")
    compileOnly(project(":Core"))
}

