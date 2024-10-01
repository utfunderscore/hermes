plugins {
    kotlin("jvm") version "2.0.0"
    id("maven-publish")
}

group = "org.readutf.hermes"

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("com.esotericsoftware:kryo5:5.6.0")
    compileOnly(project(":Core"))
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
            artifactId = "kryo"
            version = project.version.toString()

            from(components["java"])
        }
    }
}

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
