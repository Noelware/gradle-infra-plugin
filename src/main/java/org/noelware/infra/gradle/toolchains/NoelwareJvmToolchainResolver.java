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

package org.noelware.infra.gradle.toolchains;

import java.net.URI;
import java.util.Optional;
import org.gradle.jvm.toolchain.JavaToolchainDownload;
import org.gradle.jvm.toolchain.JavaToolchainRequest;
import org.gradle.jvm.toolchain.JavaToolchainResolver;
import org.gradle.jvm.toolchain.JavaToolchainSpec;
import org.jetbrains.annotations.NotNull;
import org.noelware.infra.gradle.OperatingSystem;

/**
 * Represents Noelware's JVM toolchain resolver based off the requirements that the request
 * is giving us.
 * <p>
 * Since Noelware only uses Eclipse Temurin distributions, we will only opt in with this one,
 * but we do plan to update this if anyone else (except Noelware) uses it.
 *
 * @author Noelware (team@noelware.org)
 * @since 17.02.23
 * @see org.gradle.jvm.toolchain.JavaToolchainResolver
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class NoelwareJvmToolchainResolver implements JavaToolchainResolver {
    @Override
    @NotNull
    public Optional<JavaToolchainDownload> resolve(@NotNull JavaToolchainRequest request) {
        // We only use the Temurin distribution for JDK, so that's what we will opt into. If anyone
        // else uses this, maybe we can support other distributions that isn't Adoptium! Just
        // submit a pull request.
        final JavaToolchainSpec javaToolchainSpec = request.getJavaToolchainSpec();
        final OperatingSystem current = OperatingSystem.current();

        return Optional.of(URI.create(
                        "https://api.foojay.io/disco/v3.0/packages?jdk_version=%s&distro=temurin&operating_system=%s"
                                .formatted(
                                        javaToolchainSpec.getLanguageVersion().get(),
                                        current.isMacOS() ? "darwin" : current.getName())))
                .map(JavaToolchainDownload::fromUri);
    }
}
