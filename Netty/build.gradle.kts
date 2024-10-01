plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "org.readutf.hermes"

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("io.netty:netty-all:4.1.111.Final")
    compileOnly(project(":Core"))
}

publishing {
    repositories {
        maven {
            name = "utfunderscore"
            url =
                if (version.toString().contains("SNAPSHOT")) {
                    uri("https://repo.readutf.org/snapshots")
                } else {
                    uri("https://repo.readutf.org/releases")
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
            artifactId = "netty"
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
