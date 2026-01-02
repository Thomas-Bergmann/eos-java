plugins {
    `java-library`
    jacoco
    id("io.quarkus")
}

dependencies {
    implementation(project(":simulation"))
    implementation(project(":units"))

    implementation("org.slf4j:slf4j-api")

    implementation("jakarta.inject:jakarta.inject-api")

    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    implementation("io.quarkus:quarkus-arc")

    testImplementation(project(":persistence-memory"))
    testImplementation(project(":metrics-memory"))
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
