plugins {
    id("java")
}

group = "org.readutf.hermes"
version = "dev"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":kryo"))
    testImplementation(project(":core"))
    testImplementation("com.esotericsoftware:kryo:5.2.0")
    testImplementation("io.netty:netty-all:4.1.119.Final")
    testImplementation("org.tinylog:tinylog-api:2.7.0")
    testImplementation("org.tinylog:tinylog-impl:2.7.0")
    testImplementation("org.tinylog:slf4j-tinylog:2.7.0")

    compileOnly("io.netty:netty-all:4.1.119.Final")
    compileOnly(project(":core"))
}

tasks.test {
    useJUnitPlatform()
}