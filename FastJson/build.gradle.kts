plugins {
    kotlin("jvm")
    id("maven-publish")
}
dependencies {
    testImplementation(kotlin("test"))
    compileOnly(project(":Core"))
    compileOnly("com.alibaba:fastjson:2.0.51")
}