plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.readutf.hermes"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("com.esotericsoftware:kryo5:5.6.0")
    compileOnly(project(":Core"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
