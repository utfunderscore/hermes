plugins {
    kotlin("jvm")
}

group = "org.readutf.hermes"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(kotlin("test"))

    implementation("io.netty:netty-all:4.1.111.Final")

    compileOnly(project(":Core"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
