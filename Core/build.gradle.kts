plugins {
    kotlin("jvm") version "2.0.0"
    id("java-library")
    id("maven-publish")
}

group = "org.readutf.hermes"
version = "1.1.0"

dependencies {
    // logging
    api("io.github.oshai:kotlin-logging-jvm:5.1.0")

    // Expressible
    api("org.panda-lang:expressible:1.3.6") // Core library
    api("org.panda-lang:expressible-kt:1.3.6") // Kotlin extensions

    // Reflection
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
}

publishing {
    repositories {
        maven {
            name = "utfunderscore"
            url = uri("https://reposilite.readutf.org/releases")
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
