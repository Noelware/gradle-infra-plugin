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

public class KotlinModulePluginTests {
    @TempDir
    private File testProjectDir;

    @BeforeEach
    public void setup() throws IOException {
        final File settingsGradleKts = new File(testProjectDir, "settings.gradle.kts");
        FileUtils.writeFile(settingsGradleKts, """
        rootProject.name = "test-project-1"
        """);

        final File buildGradleKts = new File(testProjectDir, "build.gradle.kts");
        FileUtils.writeFile(
                buildGradleKts,
                """
        plugins {
            id("org.noelware.gradle.kotlin")
            kotlin("jvm") version "1.8.10"
        }

        description = "a test project"
        noelware {
            minimumJavaVersion.set(org.gradle.api.JavaVersion.VERSION_17)
            projectDescription.set("a test project lmao")
            projectEmoji.set("\uD83D\uDC3B\u200D❄️\uD83D\uDC18")
            projectName.set("test-project")
            license.set(org.noelware.infra.gradle.Licenses.MIT)
        }

        repositories {
            mavenCentral()
        }
        """);

        final File kotlinFile = new File(testProjectDir, "src/main/kotlin/Main.kt");
        FileUtils.writeFile(
                kotlinFile,
                """
        import kotlin.TODO

        fun main(args: Array<String>) {
            TODO("am i cute?")
        }
        """);
    }

    @Test
    public void test_canWeRunSpotlessCorrectly() throws IOException {
        assertDoesNotThrow(() -> GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("spotlessApply")
                .withPluginClasspath()
                .forwardOutput()
                .build());

        final String content = FileUtils.readFile(new File(testProjectDir, "src/main/kotlin/Main.kt"));
        assertEquals(
                """
/*
 * \uD83D\uDC3B\u200D❄️\uD83D\uDC18 test-project: a test project lmao
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

import kotlin.TODO

fun main(args: Array<String>) {
    TODO("am i cute?")
}
        """,
                content);

        // Reset the progress and let's test with Apache 2.0
        final File kotlinFile = new File(testProjectDir, "src/main/kotlin/Main.kt");
        FileUtils.writeFile(
                kotlinFile,
                """
        import kotlin.TODO

        fun main(args: Array<String>) {
            TODO("am i cute?")
        }
        """);

        final File buildGradleKts = new File(testProjectDir, "build.gradle.kts");
        FileUtils.writeFile(
                buildGradleKts,
                """
        plugins {
            id("org.noelware.gradle.kotlin")
            kotlin("jvm") version "1.8.10"
        }

        description = "a test project"
        noelware {
            minimumJavaVersion.set(org.gradle.api.JavaVersion.VERSION_17)
            projectDescription.set("a test project lmao")
            projectEmoji.set("\uD83D\uDC3B\u200D❄️\uD83D\uDC18")
            projectName.set("test-project")
            license.set(org.noelware.infra.gradle.Licenses.APACHE)
        }

        repositories {
            mavenCentral()
        }
        """);

        assertDoesNotThrow(() -> GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("spotlessApply")
                .withPluginClasspath()
                .forwardOutput()
                .build());

        final String contentAgain = FileUtils.readFile(new File(testProjectDir, "src/main/kotlin/Main.kt"));
        assertEquals(
                """
/*
 * \uD83D\uDC3B\u200D❄️\uD83D\uDC18 test-project: a test project lmao
 * Copyright 2023 Noelware, LLC. <team@noelware.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import kotlin.TODO

fun main(args: Array<String>) {
    TODO("am i cute?")
}
        """,
                contentAgain);
    }
}
