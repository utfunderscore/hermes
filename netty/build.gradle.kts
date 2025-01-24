plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("io.netty:netty-all:4.2.0.RC1")
    compileOnly(project(":core"))
}
