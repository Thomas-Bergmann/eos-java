plugins {
    java
    id("io.quarkus")
}

dependencies {
    implementation(project(":units"))

    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.slf4j:slf4j-api")

    testImplementation("io.quarkus:quarkus-junit5")
}
