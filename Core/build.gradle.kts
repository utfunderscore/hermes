plugins {
    kotlin("jvm") version "2.0.0"
    id("java-library")
    id("maven-publish")
}

group = "org.readutf.hermes"

dependencies {
    // Add kotlin test
    testImplementation(kotlin("test"))

    // logging
    api("io.github.oshai:kotlin-logging-jvm:5.1.0")

    // Reflection
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")

    testRuntimeOnly("org.apache.logging.log4j:log4j-api:2.14.1")
    testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")
}

publishing {
    repositories {
        maven {
            name = "utfunderscore"
            if (version.toString().contains("SNAPSHOT")) {
                url = uri("https://repo.readutf.org/snapshots")
            } else {
                url = uri("https://repo.readutf.org/releases")
            }
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.readutf.hermes"
            artifactId = "core"
            version = project.version.toString()

            from(components["java"])
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
