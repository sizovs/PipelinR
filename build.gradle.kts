buildscript {
    repositories {
        jcenter()
        mavenLocal()
        mavenCentral()
    }
}

group = "an.awesome"
version = project.findProperty("version") ?: "0.0.3"

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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
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