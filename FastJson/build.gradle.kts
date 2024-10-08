plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "org.readutf.hermes"

dependencies {
    testImplementation(kotlin("test"))
    compileOnly(project(":Core"))
    compileOnly("com.alibaba:fastjson:2.0.51")
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
            artifactId = "fastjson"
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
