buildscript {
    repositories {
        jcenter()
        mavenLocal()
        mavenCentral()
    }
}

group = "an.awesome"
version = project.findProperty("version") ?: "UNSPECIFIED"

plugins {
    java
    jacoco
    id("maven-publish")
    id("io.spring.bintray") version "0.11.1"
}


tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
}

val projectUrl = "https://github.com/sizovs/pipelinr"
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            pom {
                name.set("PipelinR")
                description.set("A lightweight command processing pipeline ❍ ⇢ ❍ ⇢ ❍ for your Java awesome app.")
                url.set(projectUrl)
            }
//            pom.withXml {
//                val root = asNode()
//                val url = "https://github.com/sizovs/pipelinr"
//                root.appendNode("name", "PipelinR")
//                root.appendNode("description", "A lightweight command processing pipeline ❍ ⇢ ❍ ⇢ ❍ for your Java awesome app.")
//                root.appendNode("url", url)
//            }
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

tasks {
    named<JacocoReport>("jacocoTestReport") {
        reports {
            xml.isEnabled = true
            html.isEnabled = false
        }
    }
    test {
        useJUnitPlatform()
    }
    check {
        dependsOn(jacocoTestReport)
    }

}

bintray {
    bintrayUser = project.findProperty("bintrayUser") as String?
    bintrayKey = project.findProperty("bintrayKey") as String?
    org = project.findProperty("bintrayUser") as String?
    repo = "maven"
    publication = "mavenJava"
    licenses = listOf("MIT")
}



repositories {
    jcenter()
    mavenLocal()
    mavenCentral()
}


java {
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