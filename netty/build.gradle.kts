plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(project(":jackson"))
    testImplementation("io.netty:netty-all:4.1.119.Final")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    testImplementation(project(":core"))
    testImplementation("org.tinylog:tinylog-api:2.7.0")
    testImplementation("org.tinylog:tinylog-impl:2.7.0")
    testImplementation("org.tinylog:slf4j-tinylog:2.7.0")

    compileOnly("io.netty:netty-all:4.1.119.Final")
    compileOnly(project(":core"))
}
