buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

group = "net.sizovs"
version = project.findProperty("version") ?: "UNSPECIFIED"

plugins {
    `java-library`
    jacoco
    id("com.diffplug.spotless") version "6.12.0"
    id("io.github.sgtsilvio.gradle.maven-central-publishing") version "0.4.1"
    id("io.github.sgtsilvio.gradle.metadata") version "0.6.0"
}

spotless {
    java {
        googleJavaFormat()
    }
}

val projectUrl = "https://github.com/sizovs/pipelinr"

publishing {
    publications {
        register<MavenPublication>("main") {
            from(components["java"])
            pom {
                name.set("PipelinR")
                description.set("A lightweight command processing pipeline ❍ ⇢ ❍ ⇢ ❍ for your Java awesome app.")
                url.set(projectUrl)
            }
            pom.licenses {
                license {
                    name.set("MIT License")
                    url.set(projectUrl)
                    distribution.set("repo")
                }
            }
            pom.developers {
                developer {
                    id.set("sizovs")
                    name.set("Eduards Sizovs")
                    email.set("eduards@sizovs.net")
                }
            }
            pom.scm {
                url.set(projectUrl)
                connection.set(projectUrl)
                developerConnection.set(projectUrl)
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["main"])
}

tasks {
    named<JacocoReport>("jacocoTestReport") {
        reports {
            xml.required.set(true)
            html.required.set(false)
        }
    }
    test {
        useJUnitPlatform()
    }
    check {
        dependsOn(jacocoTestReport)
    }

}

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.0")
    testImplementation("org.junit.platform:junit-platform-runner:1.4.0")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("org.mockito:mockito-core:2.24.0")
    testImplementation("org.mockito:mockito-junit-jupiter:2.24.0")
}
