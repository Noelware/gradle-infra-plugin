/*
 * 🐻‍❄️🐘 gradle-plugin: Gradle plugin to configure sane defaults for Noelware's Gradle projects
 * Copyright (c) 2023 Noelware, LLC. <team@noelware.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.noelware.infra.gradle.*
import dev.floofy.utils.gradle.*
import java.io.StringReader
import java.util.Properties

plugins {
    id("com.diffplug.spotless")

    `java-gradle-plugin`
    `maven-publish`
    java
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    noel()
}

dependencies {
    implementation("com.gradle.enterprise:com.gradle.enterprise.gradle.plugin:3.12.3")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.15.0")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.7.20")
    implementation("dev.floofy.commons:gradle:2.5.0")
    implementation(kotlin("gradle-plugin", "1.8.10"))
    implementation(gradleApi())

    // test dependencies (Gradle TestKit + JUnit5)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation(gradleTestKit())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

spotless {
    java {
        licenseHeaderFile("${rootProject.projectDir}/assets/HEADING")
        trimTrailingWhitespace()
        removeUnusedImports()
        palantirJavaFormat()
        endWithNewline()
    }
}

gradlePlugin {
    isAutomatedPublishing = false

    @Suppress("UnstableApiUsage")
    vcsUrl by "https://github.com/Noelware/gradle-infra"

    @Suppress("UnstableApiUsage")
    website by "https://docs.noelware.org/libraries/gradle-infra/$VERSION"
    plugins {
        create("kotlin-module") {
            implementationClass = "org.noelware.infra.gradle.plugins.module.KotlinModulePlugin"
            id = "org.noelware.gradle.kotlin"
        }

        create("java-module") {
            implementationClass = "org.noelware.infra.gradle.plugins.module.JavaModulePlugin"
            id = "org.noelware.gradle.java"
        }

        create("kotlin-library") {
            implementationClass = "org.noelware.infra.gradle.plugins.module.KotlinLibraryPlugin"
            id = "org.noelware.gradle.kotlin-library"
        }

        create("java-library") {
            implementationClass = "org.noelware.infra.gradle.plugins.module.JavaLibraryPlugin"
            id = "org.noelware.gradle.java-library"
        }

        create("settings") {
            implementationClass = "org.noelware.infra.gradle.plugins.settings.NoelwareSettingsPlugin"
            id = "org.noelware.gradle.settings"
        }
    }
}

tasks {
    withType<Jar> {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Version" to "$VERSION",
                    "Implementation-Vendor" to "Noelware, LLC. [team@noelware.org]",
                    "Implementation-Title" to "gradle-infra-plugin"
                )
            )
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
        outputs.upToDateWhen { false }
        maxParallelForks = Runtime.getRuntime().availableProcessors()
        failFast = true // kill gradle if a test fails

        testLogging {
            events.addAll(listOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED))
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

// Get the `publishing.properties` file from the `gradle/` directory
// in the root project.
val publishingPropsFile = file("${rootProject.projectDir}/gradle/publishing.properties")
val publishingProps = Properties()

// If the file exists, let's get the input stream
// and load it.
if (publishingPropsFile.exists()) {
    publishingProps.load(publishingPropsFile.inputStream())
} else {
    // Check if we do in environment variables
    val accessKey = System.getenv("NOELWARE_PUBLISHING_ACCESS_KEY") ?: ""
    val secretKey = System.getenv("NOELWARE_PUBLISHING_SECRET_KEY") ?: ""

    if (accessKey.isNotEmpty() && secretKey.isNotEmpty()) {
        val data = """
        |s3.accessKey=$accessKey
        |s3.secretKey=$secretKey
        """.trimMargin()

        publishingProps.load(StringReader(data))
    }
}

// Check if we have the `NOELWARE_PUBLISHING_ACCESS_KEY` and `NOELWARE_PUBLISHING_SECRET_KEY` environment
// variables, and if we do, set it in the publishing.properties loader.
val snapshotRelease: Boolean = run {
    val env = System.getenv("NOELWARE_PUBLISHING_IS_SNAPSHOT") ?: "false"
    env == "true"
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier by "sources"
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assemble Kotlin documentation with Dokka"

    archiveClassifier by "javadoc"
    from(tasks.javadoc)
    dependsOn(tasks.javadoc)
}

publishing {
    publications {
        create<MavenPublication>("ktor") {
            from(components["java"])

            artifactId = "gradle-infra-plugin"
            groupId = "org.noelware.gradle"
            version = "$VERSION"

            artifact(sourcesJar.get())
            artifact(javadocJar.get())

            pom {
                description by "Gradle plugin to configure sane defaults for Noelware's Gradle projects"
                name by "gradle-infra-plugin"
                url by "https://docs.noelware.org/libraries/java/gradle-infra-plugin/$VERSION"

                organization {
                    name by "Noelware"
                    url by "https://noelware.org"
                }

                developers {
                    developer {
                        name by "Noel"
                        email by "cutie@floofy.dev"
                        url by "https://floofy.dev"
                    }

                    developer {
                        name by "Noelware Team"
                        email by "team@noelware.org"
                        url by "https://noelware.org"
                    }
                }

                issueManagement {
                    system by "GitHub"
                    url by "https://github.com/Noelware/gradle-infra-plugin/issues"
                }

                licenses {
                    license {
                        name by "MIT License"
                        url by "https://github.com/Noelware/gradle-infra-plugin/blob/master/LICENSE"
                    }
                }

                scm {
                    connection by "scm:git:ssh://github.com/Noelware/gradle-infra-plugin.git"
                    developerConnection by "scm:git:ssh://git@github.com:Noelware/gradle-infra-plugin.git"
                    url by "https://github.com/Noelware/gradle-infra-plugin"
                }
            }
        }
    }

    repositories {
        val url = if (snapshotRelease) "s3://august/noelware/maven/snapshots" else "s3://august/noelware/maven"
        maven(url) {
            credentials(AwsCredentials::class.java) {
                accessKey = publishingProps.getProperty("s3.accessKey") ?: System.getenv("NOELWARE_PUBLISHING_ACCESS_KEY") ?: ""
                secretKey = publishingProps.getProperty("s3.secretKey") ?: System.getenv("NOELWARE_PUBLISHING_SECRET_KEY") ?: ""
            }
        }
    }
}
