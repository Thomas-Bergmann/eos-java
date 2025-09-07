plugins {
    `java-library`
    jacoco
    id("io.quarkus")
}

dependencies {
    implementation("org.slf4j:slf4j-api")
    implementation("org.apache.commons:commons-csv")

    implementation("jakarta.enterprise:jakarta.enterprise.cdi-api")
    implementation("jakarta.inject:jakarta.inject-api")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api")

    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-scheduler")

    // Prometheus metrics
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    
    // InfluxDB client for Grafana visualization
    implementation("com.influxdb:influxdb-client-java:7.3.0")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
