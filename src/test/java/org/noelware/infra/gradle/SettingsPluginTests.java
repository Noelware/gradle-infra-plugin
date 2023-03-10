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

package org.noelware.infra.gradle;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.noelware.infra.gradle.utils.FileUtils;

public class SettingsPluginTests {
    @TempDir
    File testProjectDir;

    File settingsKtsFile;
    File settingsFile;

    @BeforeEach
    public void setup() {
        settingsKtsFile = new File(testProjectDir, "settings.gradle.kts");
        settingsFile = new File(testProjectDir, "settings.gradle");
    }

    @Test
    public void test_settingsPlugin() throws IOException {
        FileUtils.writeFile(
                settingsFile,
                """
        plugins {
          id("org.noelware.gradle.settings")
        }

        rootProject.name = 'test'
        gradleEnterprise {}
        """);

        assertDoesNotThrow(() -> GradleRunner.create()
                .forwardOutput()
                .withProjectDir(testProjectDir)
                .withPluginClasspath()
                .build());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void test_settingsPluginKotlin() throws IOException {
        settingsFile.delete();
        FileUtils.writeFile(
                settingsKtsFile,
                """
        plugins {
            id("org.noelware.gradle.settings")
        }

        logger.lifecycle("hi")
        rootProject.name = "test"
        """);

        assertDoesNotThrow(() -> GradleRunner.create()
                .forwardOutput()
                .withProjectDir(testProjectDir)
                .withPluginClasspath()
                .build());

        // Check all provisioned JVMs
        assertDoesNotThrow(() -> GradleRunner.create()
                .forwardOutput()
                .withArguments("-q", "javaToolchains")
                .withProjectDir(testProjectDir)
                .withPluginClasspath()
                .build());
    }
}
