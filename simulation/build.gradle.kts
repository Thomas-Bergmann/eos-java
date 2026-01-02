plugins {
    `java-library`
    jacoco
    id("io.quarkus")
}

dependencies {
    implementation(project(":units"))
    implementation(project(":persistence-api"))

    implementation("org.slf4j:slf4j-api")
    implementation("org.apache.commons:commons-csv")

    implementation("jakarta.inject:jakarta.inject-api")
    implementation("jakarta.validation:jakarta.validation-api")

    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("io.quarkus:quarkus-arc")

    testImplementation(project(":persistence-memory"))
    testImplementation(project(":metrics-memory"))
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
