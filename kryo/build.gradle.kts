plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("com.esotericsoftware:kryo:5.6.2")
    compileOnly(project(":core"))
}

tasks.test {
    useJUnitPlatform()
}