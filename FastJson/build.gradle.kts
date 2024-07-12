plugins {
    kotlin("jvm")
}

group = "org.readutf.hermes"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(kotlin("test"))
    compileOnly(project(":Core"))
    compileOnly("com.alibaba:fastjson:2.0.51")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
