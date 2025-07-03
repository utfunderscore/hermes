plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":kryo"))
    testImplementation("com.esotericsoftware:kryo:5.6.2")
    testImplementation("org.tinylog:tinylog-api:2.7.0")
    testImplementation("org.tinylog:tinylog-impl:2.7.0")
    testImplementation("org.tinylog:slf4j-tinylog:2.7.0")


    api(project(":core"))
}

tasks.test {
    useJUnitPlatform()
}