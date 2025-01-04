import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.30.0"
    signing
}

group = "io.github.utfunderscore.hermes"
version = "1.0.2"

repositories {
    mavenCentral()
}

subprojects {

    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")
    apply(plugin = "com.vanniktech.maven.publish")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")

    java {
        withSourcesJar()
    }

    tasks.test {
        useJUnitPlatform()
    }
    kotlin {
        jvmToolchain(17)
    }
    repositories {
        mavenCentral()
    }

    mavenPublishing {

        coordinates(
            groupId = group.toString(),
            version = version.toString(),
            artifactId = "hermes.$name"
        )

        pom {
            name.set("Hermes")
            description.set("Multi-platform packet library")
            inceptionYear.set("2024")

            url.set("https://github.com/utfunderscore/Hermes")
            licenses {
                license {
                    name.set("GPLv3")
                    url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                    distribution.set("https://www.gnu.org/licenses/gpl-3.0.html")
                }
            }
            developers {
                developer {
                    id.set("utfunderscore")
                    name.set("utfunderscore")
                    url.set("utf.lol")
                }
            }
            scm {
                url.set("https://github.com/utfunderscore/Hermes/")
                connection.set("scm:git:git://github.com/utfunderscore/Hermes.git")
                developerConnection.set("scm:git:ssh://git@github.com/utfunderscore/Hermes.git")
            }

        }

        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
        signAllPublications()

    }


}


signing {
    sign(publishing.publications)
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
