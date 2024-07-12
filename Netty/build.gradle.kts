plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "org.readutf.hermes"
version = "1.2.0"

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("io.netty:netty-all:4.1.111.Final")
    compileOnly(project(":Core"))
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
            artifactId = "netty"
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
