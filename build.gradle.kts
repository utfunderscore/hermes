plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "org.readutf.hermes"
version = "2.0.0"

repositories {
    mavenCentral()
}

subprojects {

    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        withSourcesJar()
    }

    tasks.test {
        useJUnitPlatform()
    }
    repositories {
        mavenCentral()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                groupId = project.group as String
                artifactId = project.name
                version = project.version as String
                from(components["java"])
            }
        }

        repositories {
            maven {
                name = "utfMvn"
                url = uri("https://mvn.utf.lol/releases")
                credentials {
                    username = System.getenv("UTF_MVN_USER")
                    password = System.getenv("UTF_MVN_PASS")
                }
            }
        }
    }
}

dependencies {
}

tasks.test {
    useJUnitPlatform()
}
