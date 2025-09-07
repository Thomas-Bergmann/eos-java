plugins {
    id("java-platform")
}

// Define dependency versions for dependency groups
val fasterXMLVersion = "2.20.0"

javaPlatform.allowDependencies()

dependencies {
    api(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.24.2"))

    constraints {

        // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
        api("org.apache.commons:commons-lang3:3.17.0")
        api("org.apache.commons:commons-csv:1.14.1")

        api("com.fasterxml.jackson.core:jackson-core:${fasterXMLVersion}")
        api("com.fasterxml.jackson.core:jackson-databind:${fasterXMLVersion}")
        api("com.fasterxml.jackson.core:jackson-annotations:${fasterXMLVersion}")

        api("com.microsoft.azure:msal4j:1.21.0")
        api("com.azure:azure-identity:1.16.2")
        api("com.azure:azure-storage-blob:12.30.1")
        api("com.azure:azure-data-tables:12.5.4")
        api("com.azure:azure-storage-file-share:12.26.1")

        // Prometheus metrics
        api("io.micrometer:micrometer-registry-prometheus:1.14.3")
    }
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}
