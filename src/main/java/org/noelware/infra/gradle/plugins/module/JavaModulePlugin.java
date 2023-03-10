/*
 * 🐻‍❄️🐘 gradle-infra-plugin: Gradle plugin to configure sane defaults for Noelware's Gradle projects
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

package org.noelware.infra.gradle.plugins.module;

import com.diffplug.gradle.spotless.SpotlessExtension;
import java.io.File;
import java.io.IOException;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.logging.TestExceptionFormat;
import org.gradle.api.tasks.testing.logging.TestLogEvent;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.jetbrains.annotations.NotNull;
import org.noelware.infra.gradle.Licenses;

/**
 * Represents the base plugin for configuring Java projects.
 */
public class JavaModulePlugin implements Plugin<Project> {
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void apply(@NotNull Project project) {
        final NoelwareModuleExtension ext = project.getExtensions().findByType(NoelwareModuleExtension.class) != null
                ? project.getExtensions().findByType(NoelwareModuleExtension.class)
                : project.getExtensions().create("noelware", NoelwareModuleExtension.class);

        assert ext != null : "BUG! Extension was not created or was not found?";

        project.getPlugins().apply("java");
        project.getPlugins().apply("com.diffplug.spotless");

        // Configure Spotless
        project.getExtensions().configure(SpotlessExtension.class, (spotless) -> {
            final String license = ext.getLicense().getOrElse(Licenses.MIT).render(project, ext);
            spotless.java((java) -> {
                java.licenseHeader(license);
                java.trimTrailingWhitespace();
                java.removeUnusedImports();
                java.palantirJavaFormat();
                java.endWithNewline();
            });

            spotless.kotlinGradle(kotlin -> {
                kotlin.endWithNewline();
                kotlin.encoding("UTF-8");
                kotlin.target("**/*.gradle.kts");

                try {
                    final var ktlint = kotlin.ktlint().setUseExperimental(true);
                    if (new File(project.getRootProject().getProjectDir(), ".editorconfig").exists()) {
                        ktlint.setEditorConfigPath(
                                new File(project.getRootProject().getProjectDir(), ".editorconfig"));
                    }
                } catch (IOException e) {
                    throw new GradleException("Unable to apply Ktlint to Spotless", e);
                }

                kotlin.licenseHeader(license, "(package |@file|import |pluginManagement|plugins|rootProject.name)");
            });
        });

        // Set up the Java things
        project.getExtensions().configure(JavaPluginExtension.class, (java) -> {
            java.toolchain((toolchain) -> toolchain
                    .getLanguageVersion()
                    .set(JavaLanguageVersion.of(ext.getMinimumJavaVersion()
                            .getOrElse(JavaVersion.VERSION_17)
                            .getMajorVersion())));
        });

        // configure junit tests if needed
        project.getTasks().withType(Test.class).configureEach((test) -> {
            test.useJUnitPlatform();
            test.getOutputs().upToDateWhen((a) -> false);
            test.setMaxParallelForks(Runtime.getRuntime().availableProcessors());
            test.setFailFast(true);
            test.testLogging((logging) -> {
                logging.events(
                        TestLogEvent.PASSED,
                        TestLogEvent.FAILED,
                        TestLogEvent.SKIPPED,
                        TestLogEvent.STANDARD_ERROR,
                        TestLogEvent.STANDARD_OUT,
                        TestLogEvent.STARTED);

                logging.setShowCauses(true);
                logging.setShowStandardStreams(true);
                logging.setShowExceptions(true);
                logging.setExceptionFormat(TestExceptionFormat.FULL);
            });
        });
    }
}
