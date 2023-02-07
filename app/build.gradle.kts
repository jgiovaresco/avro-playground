plugins {
    application
    kotlin("jvm") version "1.8.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vertx:vertx-web-client:4.2.7")
    implementation("org.apache.avro:avro:1.11.1")

    // Use JUnit Jupiter for testing.
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.testcontainers:testcontainers:1.17.6")
    testImplementation("org.testcontainers:junit-jupiter:1.17.6")
    testImplementation("io.vertx:vertx-junit5:4.2.7")

}

application {
    // Define the main class for the application.
    mainClass.set("avro.playground.App")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
