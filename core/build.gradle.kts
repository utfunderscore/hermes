plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    api("org.jetbrains:annotations:24.1.0")
    api("org.slf4j:slf4j-api:2.0.9")
}

tasks.test {
    useJUnitPlatform()
}