plugins {
    kotlin("jvm") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.30.0"
    signing
}

group = "io.github.utfunderscore.hermes"
version = "1.0.3"

repositories {
    mavenCentral()
}

subprojects {

    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")
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

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "org.readutf.hermes"
                artifactId = "hermes.${project.name}"
                version = rootProject.version.toString()

                from(components["java"])
            }
        }
    }
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
