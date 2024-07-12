plugins {
    kotlin("jvm") version "2.0.0"
    id("maven-publish")
}

group = "org.readutf.hermes"
version = "1.1.0"

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("com.esotericsoftware:kryo5:5.6.0")
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
            artifactId = "kryo"
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
