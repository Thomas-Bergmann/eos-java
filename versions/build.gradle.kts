plugins {
    id("java-platform")
}

// Define dependency versions for dependency groups
val fasterXMLVersion = "2.20.0"

javaPlatform.allowDependencies()

dependencies {
    api(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.30.5"))

    constraints {

        api("org.apache.commons:commons-lang3:3.20.0")
        api("org.apache.commons:commons-csv:1.14.1")

        api("com.fasterxml.jackson.core:jackson-core:${fasterXMLVersion}")
        api("com.fasterxml.jackson.core:jackson-databind:${fasterXMLVersion}")
        api("com.fasterxml.jackson.core:jackson-annotations:${fasterXMLVersion}")

        // time/metrics export
        api("com.influxdb:influxdb-client-java:7.4.0")

    }
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}
