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
import java.util.Calendar;
import java.util.List;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.logging.TestExceptionFormat;
import org.gradle.api.tasks.testing.logging.TestLogEvent;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.gradle.dsl.JvmTarget;
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile;
import org.noelware.infra.gradle.Licenses;

/**
 * Represents the base plugin for configuring Kotlin projects.
 */
public class KotlinModulePlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) {
        final Logger log = project.getLogger();
        final NoelwareModuleExtension ext = project.getExtensions().findByType(NoelwareModuleExtension.class) != null
                ? project.getExtensions().findByType(NoelwareModuleExtension.class)
                : project.getExtensions().create("noelware", NoelwareModuleExtension.class);

        assert ext != null : "extension couldn't be created";

        final JavaVersion javaVersion = ext.getMinimumJavaVersion().getOrElse(JavaVersion.VERSION_17);

        log.info("Initializing Kotlin module...");
        project.getPlugins().apply("org.jetbrains.kotlin.jvm");
        project.getPlugins().apply("com.diffplug.spotless");

        if (project.getPlugins().hasPlugin("org.jetbrains.kotlin.plugin.serialization")) {
            project.getPlugins().apply("org.jetbrains.kotlin.plugin.serialization");
        }

        // Configure Spotless
        project.getExtensions().configure(SpotlessExtension.class, (spotless) -> {
            spotless.kotlin((kotlin) -> {
                try {
                    kotlin.licenseHeader(ext.getLicense()
                            .convention(Licenses.MIT)
                            .get()
                            .getTemplate(
                                    ext.getProjectName()
                                            .getOrElse(project.getRootProject().getName()),
                                    ext.getProjectDescription()
                                            .getOrElse(
                                                    project.getDescription() != null
                                                            ? project.getDescription()
                                                            : "fill this out"),
                                    ext.getCurrentYear()
                                            .getOrElse(String.valueOf(
                                                    Calendar.getInstance().get(Calendar.YEAR))),
                                    ext.getProjectEmoji().getOrElse("")));

                    kotlin.trimTrailingWhitespace();
                    kotlin.endWithNewline();
                    kotlin.encoding("UTF-8");
                    kotlin.ktlint()
                            .setUseExperimental(true)
                            .setEditorConfigPath(
                                    new File(project.getRootProject().getProjectDir(), ".editorconfig"));
                } catch (IOException e) {
                    throw new GradleException("unable to generate license template", e);
                }
            });
        });

        // Set up the Java things
        project.getExtensions().configure(JavaPluginExtension.class, (java) -> {
            java.toolchain((toolchain) ->
                    toolchain.getLanguageVersion().set(JavaLanguageVersion.of(javaVersion.getMajorVersion())));
        });

        // Set up Kotlin compile tasks
        project.getTasks().withType(KotlinCompile.class).configureEach((compiler) -> {
            compiler.compilerOptions((opts) -> {
                opts.getFreeCompilerArgs().set(List.of("-opt-in=kotlin.RequiresOptIn"));
                opts.getJavaParameters().set(true);
                opts.getJvmTarget().set(JvmTarget.Companion.fromTarget(javaVersion.getMajorVersion()));
            });
        });

        // configure junit tests if needed
        if (ext.getUnitTests().getOrElse(false)) {
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
                    logging.setShowExceptions(true);
                    logging.setExceptionFormat(TestExceptionFormat.FULL);
                });
            });
        }
    }
}
