import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    java
    groovy
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

repositories {
    jcenter()
    mavenCentral()
}

group = "pipelinr"
version = "UNSPECIFIED"


java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}


tasks{
    val relocateShadowJar by registering(ConfigureShadowRelocation::class) {
        target = shadowJar.get()
        prefix = "org.repackage"
    }

    shadowJar {
        dependsOn(relocateShadowJar)
    }

}


dependencies {

    implementation("one.util:streamex:0.6.8")
    implementation("com.google.guava:guava:27.0.1-jre")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.0")
    testImplementation("org.mockito:mockito-junit-jupiter:2.24.0")
    testImplementation("org.assertj:assertj-core:3.11.1")

}


