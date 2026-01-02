plugins {
    `java-library`
    jacoco
    id("io.quarkus")
}

dependencies {
    implementation(project(":units"))
    implementation(project(":persistence-api"))

    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("io.quarkus:quarkus-arc")
    implementation("org.apache.commons:commons-csv")
    implementation("org.slf4j:slf4j-api")
    implementation("jakarta.inject:jakarta.inject-api")

    testImplementation(project(":persistence-memory"))
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
