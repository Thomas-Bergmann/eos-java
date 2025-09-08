plugins {
    `java-library`
    jacoco
    id("io.quarkus")
}

dependencies {
    implementation("org.slf4j:slf4j-api")
    implementation("org.apache.commons:commons-csv")

    implementation("jakarta.inject:jakarta.inject-api")
    implementation("jakarta.validation:jakarta.validation-api")

    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    implementation("io.quarkus:quarkus-arc")

    // InfluxDB client for Grafana visualization
    implementation("com.influxdb:influxdb-client-java")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
