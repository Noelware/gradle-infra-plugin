/*
 * {{ Emoji }} gradle-infra: fill this out
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

import dev.floofy.utils.gradle.*
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.noelware.infra.gradle.*

plugins {
    id("org.noelware.gradle.java-library")
    `java-gradle-plugin`
    `kotlin-dsl`
}

group = "org.noelware.gradle"
description = "\uD83D\uDC3B\u200D❄️\uD83D\uDC18 Gradle plugin to configure sane defaults for Noelware's Gradle projects"
version = "$VERSION"

// Check if we have the `NOELWARE_PUBLISHING_ACCESS_KEY` and `NOELWARE_PUBLISHING_SECRET_KEY` environment
// variables, and if we do, set it in the publishing.properties loader.
val snapshotRelease: Boolean = run {
    val env = System.getenv("NOELWARE_PUBLISHING_IS_SNAPSHOT") ?: "false"
    env == "true"
}

noelware {
    mavenPublicationName by "gradle-infra"
    minimumJavaVersion by JAVA_VERSION
    projectDescription by "Gradle plugin to configure sane defaults for Noelware's Gradle projects"
    projectEmoji by "\uD83D\uDC3B\u200D❄️\uD83D\uDC18"
    projectName by "gradle-infra-plugin"
    s3BucketUrl by if (snapshotRelease) "s3://august/noelware/maven/snapshots" else "s3://august/noelware/maven"
    license by Licenses.MIT
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    noel()
}

dependencies {
    implementation("com.gradle.enterprise:com.gradle.enterprise.gradle.plugin:3.12.3")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.16.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.7.20")
    implementation("org.apache.commons:commons-text:1.10.0")
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

gradlePlugin {
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
            implementationClass = "org.noelware.infra.gradle.plugins.library.KotlinLibraryPlugin"
            id = "org.noelware.gradle.kotlin-library"
        }

        create("java-library") {
            implementationClass = "org.noelware.infra.gradle.plugins.library.JavaLibraryPlugin"
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
                    "Implementation-Title" to "gradle-infra-plugin",
                ),
            )
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
        outputs.upToDateWhen { false }

        maxParallelForks = Runtime.getRuntime().availableProcessors()
        failFast = true
        testLogging {
            events(
                TestLogEvent.PASSED,
                TestLogEvent.FAILED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STANDARD_OUT,
                TestLogEvent.STARTED,
            )

            exceptionFormat = TestExceptionFormat.FULL
            showStackTraces = true
            showExceptions = true
            showCauses = true
        }
    }
}

// publishing {
//    publications {
//        named<MavenPublication>("gradle-infra") {
//            groupId = "org.noelware.gradle"
//            artifactId = "gradle-infra-plugin"
//            version = "$VERSION"
//
//            pom {
//                description by "\uD83D\uDC3B\u200D❄️\uD83D\uDC18 Gradle plugin to configure sane defaults for Noelware's Gradle projects"
//                name by "gradle-infra-plugin"
//                url by "https://docs.noelware.org/libraries/java/gradle-infra-plugin/$VERSION"
//
//                organization {
//                    name by "Noelware, LLC."
//                    url by "https://noelware.org"
//                }
//
//                developers {
//                    developer {
//                        email by "team@noelware.org"
//                        name by "Noelware Team"
//                        url by "https://noelware.org"
//                    }
//
//                    developer {
//                        email by "cutie@floofy.dev"
//                        name by "Noel"
//                        url by "https://floofy.dev"
//                    }
//                }
//
//                licenses {
//                    license {
//                        name by "MIT License"
//                        url by Licenses.MIT.url()
//                    }
//                }
//
//                issueManagement {
//                    system by "GitHub"
//                    url by "https://github.com/Noelware/gradle-infra-plugin/issues"
//                }
//
//                scm {
//                    connection by "scm:git:ssh://github.com/Noelware/gradle-infra-plugin.git"
//                    developerConnection by "scm:git:ssh://git@github.com:Noelware/gradle-infra-plugin.git"
//                    url by "https://github.com/Noelware/gradle-infra-plugin"
//                }
//            }
//        }
//    }
// }
