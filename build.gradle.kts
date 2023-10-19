//import org.jetbrains.dokka.Platform
//import java.net.URL

group = "de.vertama"
version = "2023.10.19"

object Meta {
    const val desc = "A featureless java lib only to test publishing java libs to maven central"
    const val githubRepo = "Vertama-GmbH/zero-feature-maven-central-publish-test"
    const val release = "https://s01.oss.sonatype.org/service/local/"
    const val snapshot = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
    const val license = "Apache-2.0"
    const val license_url = "https://opensource.org/licenses/Apache-2.0"


    object Author {
        val id = "dluesebrink"
        val name = "Dirk Lüsebrink"
        val email = "dev@verama.com"
        val organization = "Vertama GmbH"
        val organization_url = "https://vertama.com/"
    }
}

plugins {
    `java-library`
    `maven-publish`
    signing

    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

repositories {
    mavenCentral()
    //mavenLocal()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.apache.commons:commons-math3:3.6.1")

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation("com.google.guava:guava:32.1.1-jre")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(project.name)
                description.set(Meta.desc)
                url.set("https://github.com/${Meta.githubRepo}")
                licenses {
                    license {
                        name.set(Meta.license)
                        url.set(Meta.license_url)
                    }
                }
                developers {
                    developer {
                        id.set(Meta.Author.id)
                        name.set(Meta.Author.name)
                        email.set(Meta.Author.email)
                        organization.set(Meta.Author.organization)
                        organizationUrl.set(Meta.Author.organization_url)
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/${Meta.githubRepo}.git")
                    developerConnection.set("scm:git:git://github.com/#${Meta.githubRepo}.git")
                    url.set("https://github.com/${Meta.githubRepo}")
                }
            }
        }
    }
    // repositories {
    //     maven {
    //         // change URLs to point to your repos, e.g. http://my.org/repo
    //         // val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
    //         // val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
    //         // url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

    //         val release = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
    //         val snapshot = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    //         url = if (version.toString().endsWith("SNAPSHOT")) snapshot else release
    //         //authentication(userName: ossrhUsername, password: ossrhPassword)
    //     }
    // }
}

signing {
    //sign(publishing.publications["mavenJava"])

    val signingKey = providers.environmentVariable("GPG_SIGNING_KEY")
    val signingPassphrase = providers.environmentVariable("GPG_SIGNING_PASSPHRASE")

    if (signingKey.isPresent && signingPassphrase.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), signingPassphrase.get())
        val extension = extensions.getByName("publishing") as PublishingExtension
        sign(extension.publications)
        //sign(publishing.publications["mavenJava"])
    }
}

nexusPublishing {
    repositories {
        create("myNexus") {
            nexusUrl.set(uri(Meta.release))
            snapshotRepositoryUrl.set(uri(Meta.snapshot))
            val ossrhUsername = providers.environmentVariable("OSSRH_USERNAME")
            val ossrhPassword = providers.environmentVariable("OSSRH_PASSWORD")
            if (!ossrhUsername.isPresent || !ossrhPassword.isPresent) {
                throw GradleException(" ** username, password must be defined in ENV as: 'OSSRH_USERNAME' and 'OSSRH_PASSWORD'")
            }
            username.set(ossrhUsername.get())
            password.set(ossrhPassword.get())
        }
    }
}
