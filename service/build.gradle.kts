plugins {
    java
    id("io.quarkus")
}

dependencies {
    implementation(project(":units"))
    implementation(project(":persistence-api"))
    implementation(project(":persistence-influx"))
    implementation(project(":forecast"))
    implementation(project(":simulation"))
    implementation(project(":metrics-influx"))

    implementation("io.quarkus:quarkus-arc")
    implementation("org.slf4j:slf4j-api")
    implementation("jakarta.inject:jakarta.inject-api")
}

