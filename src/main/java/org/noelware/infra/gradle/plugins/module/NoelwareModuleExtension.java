/*
 * { Emoji }} gradle-infra-plugin: Gradle plugin to configure sane defaults for Noelware's Gradle projects
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

import org.gradle.api.JavaVersion;
import org.gradle.api.provider.Property;
import org.noelware.infra.gradle.Licenses;

/**
 * Represents the extension for the Java or Kotlin module plugins
 */
public abstract class NoelwareModuleExtension {
    /**
     * @return {@link Property<JavaVersion>} of the minimum Java version to use for the Java/Kotlin toolchain
     * language. By default, it will use Java 17 as the minimum.
     */
    public abstract Property<JavaVersion> getMinimumJavaVersion();

    /**
     * @return {@link Property<JavaVersion>} of the publication name, defaults to the project name.
     */
    public abstract Property<String> getMavenPublicationName();

    /**
     * @return {@link Property<String>} that represents the full S3 bucket URL for the library plugins, this isn't
     * required in the module plugins.
     */
    public abstract Property<String> getS3BucketUrl();

    /**
     * @return {@link Property<String>} of the current year replacement in the license template (i.e, "2022-2023")
     */
    public abstract Property<String> getCurrentYear();

    /**
     * @return {@link Property<Licenses>} of the license to use for the license heading, defaults to <code>MIT</code>
     */
    public abstract Property<Licenses> getLicense();

    /**
     * @return {@link Property<String>} of the project name for the license, defaults to the project description
     */
    public abstract Property<String> getProjectDescription();

    /**
     * @return {@link Property<String>} of the project name for the license, defaults to the root project name
     */
    public abstract Property<String> getProjectName();

    /**
     * @return {@link Property<String>} of the project emoji for the license, defaults to none
     */
    public abstract Property<String> getProjectEmoji();

    /**
     * @return {@link Property<Boolean>} if we should include unit tests with JUnit5
     */
    public abstract Property<Boolean> getUnitTests();
}
