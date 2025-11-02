plugins {
    // IDE plugin
    idea
    // Gradle base plugin
    base
    // org.cyclonedx.bom
}

val buildDir: File = project.layout.buildDirectory.asFile.get()

repositories {
    mavenCentral()
    mavenLocal()
}

group = "com.intershop.tbergmann.product-info"
version = "1.0.0-LOCAL"

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories.addAll(rootProject.repositories)

    plugins.withType<JavaPlugin> {

        extensions.getByType(JavaPluginExtension::class.java).apply {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }

        tasks {
            withType<JavaCompile> {
                options.isFork = true
                options.encoding = "UTF-8"
            }
            withType<Javadoc> {
                if (options is StandardJavadocDocletOptions) {
                    val opt = options as StandardJavadocDocletOptions
                    // without the -quiet option, the build fails
                    // opt.addStringOption("Xdoclint:none", "-quiet")
                    opt.links("https://docs.oracle.com/en/java/javase/21/docs/api/")
                    opt.encoding = "UTF-8"
                }
            }
            withType<Test> {
                useJUnitPlatform()
                environment("INFLUX_URI", System.getenv("INFLUX_URI") ?: "http://localhost:8086")
                environment("INFLUX_TOKEN", System.getenv("INFLUX_TOKEN") ?: "eos-token-invalid")
            }

            withType<JacocoReport> {
                reports {
                    xml.required.set(true)
                    html.required.set(true)
                    xml.outputLocation.set( File(project.layout.buildDirectory.asFile.get(), "reports/jacoco/jacocoTestReport.xml") )
                    html.outputLocation.set( File(project.layout.buildDirectory.asFile.get(), "reports/jacoco") )
                }
                dependsOn("test")
            }
        }

        tasks.check {
            dependsOn(tasks.withType<JacocoReport>())
        }

        dependencies {
            val implementation by configurations
            implementation(platform(project(":versions")))
            val testImplementation by configurations
            testImplementation(platform(project(":versions-test")))
        }
    }
}
