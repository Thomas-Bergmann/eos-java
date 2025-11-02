plugins {
    `java-library`
    id("io.quarkus")
}

dependencies {
    implementation(project(":units"))
    implementation(project(":persistence-api"))

    implementation("jakarta.inject:jakarta.inject-api")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-config-yaml")

    implementation("com.influxdb:influxdb-client-java")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.quarkus:quarkus-junit5")
}
