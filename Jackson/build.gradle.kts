plugins {
    kotlin("jvm")
    id("java-library")
    id("maven-publish")
}

group = "org.readutf.hermes"
version = "1.6.4"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    api(project(":Core"))
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")
}

java {
    withSourcesJar()
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
            artifactId = "jackson"
            version = project.version.toString()

            from(components["java"])
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
