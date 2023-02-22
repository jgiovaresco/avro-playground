plugins {
    application
    kotlin("jvm") version "1.8.10"
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://packages.confluent.io/maven/")
        name = "Confluent"
        content {
            includeGroup("io.confluent")
            includeGroup("org.apache.kafka")
        }
    }
}

dependencies {
    implementation("io.confluent:kafka-avro-serializer:5.3.0")
    implementation("org.apache.kafka:kafka-clients:3.4.0")
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
