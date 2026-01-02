plugins {
    `java-library`
}

dependencies {
    implementation(project(":units"))
    implementation(project(":persistence-api"))

    implementation("org.slf4j:slf4j-api")
    implementation("jakarta.inject:jakarta.inject-api")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

