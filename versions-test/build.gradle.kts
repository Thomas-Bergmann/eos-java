plugins {
    `java-platform`
}

// Define dependency versions for dependency groups
val junitVersion = "6.0.1"
val junitPlatformVersion = "1.14.1"

dependencies {
    constraints {
        api("org.junit.jupiter:junit-jupiter:${junitVersion}")
        api("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
        api("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
        api("org.junit.jupiter:junit-jupiter-params:${junitVersion}")

        api("org.junit.platform:junit-platform-commons:${junitPlatformVersion}")
        api("org.junit.platform:junit-platform-runner:${junitPlatformVersion}")
    }
}
