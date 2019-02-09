

val springVersion = "2.1.2.RELEASE"

dependencies {

    implementation(project(":pipelinr"))
    implementation("org.springframework:spring-context:5.1.4.RELEASE")
//    implementation("org.springframework.boot:spring-boot-starter:$springVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.0")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springVersion")

}
