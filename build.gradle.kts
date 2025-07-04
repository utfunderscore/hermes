plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "org.readutf.hermes"
version = "2.0.5"

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java")

    group = rootProject.group
    version = rootProject.version

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withJavadocJar()
        withSourcesJar()
    }

    apply(plugin = "maven-publish")

    publishing {
        publications {
            create<MavenPublication>("maven") {
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
                    username = System.getenv("UTF_MVN_USER") ?: findProperty("utfMvnUser")?.toString() ?: ""
                    password = System.getenv("UTF_MVN_PASS") ?: findProperty("utfMvnPass")?.toString() ?: ""
                }
            }

        }

    }


}

tasks.test {
    useJUnitPlatform()
}
