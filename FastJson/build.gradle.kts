plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "org.readutf.hermes"
version = "1.2.2"

dependencies {
    testImplementation(kotlin("test"))
    compileOnly(project(":Core"))
    compileOnly("com.alibaba:fastjson:2.0.51")
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
            artifactId = "fastjson"
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
