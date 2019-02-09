buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
}

group = "pipelinr"
version = "UNSPECIFIED"


plugins {
    java
}

subprojects {
    apply<JavaPlugin>()
    repositories {
        jcenter()
        mavenCentral()
    }


    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
