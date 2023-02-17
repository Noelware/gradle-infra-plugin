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
package org.noelware.infra.gradle.plugins.settings;

import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension;
import com.gradle.enterprise.gradleplugin.GradleEnterprisePlugin;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.caching.http.HttpBuildCache;
import org.gradle.jvm.toolchain.JavaToolchainResolverRegistry;
import org.jetbrains.annotations.NotNull;
import org.noelware.infra.gradle.Architecture;
import org.noelware.infra.gradle.OperatingSystem;
import org.noelware.infra.gradle.toolchains.NoelwareJvmToolchainResolver;

/**
 * Represents a {@link Plugin<Settings>} for configuring the settings initialization for Noelware's
 * projects.
 */
@SuppressWarnings("UnstableApiUsage")
public class NoelwareSettingsPlugin implements Plugin<Settings> {
    private static final Pattern BOOLEAN_REGEX = Pattern.compile("^(yes|true|1|si|si*)$");
    private final JavaToolchainResolverRegistry javaToolchainResolverRegistry;

    @Inject
    public NoelwareSettingsPlugin(JavaToolchainResolverRegistry javaToolchainResolverRegistry) {
        this.javaToolchainResolverRegistry = javaToolchainResolverRegistry;
    }

    @Override
    public void apply(@NotNull Settings settings) {
        javaToolchainResolverRegistry.register(NoelwareJvmToolchainResolver.class);

        // Add the plugins that we use
        settings.getPlugins().apply("jvm-toolchain-management");
        settings.getPluginManager().apply(GradleEnterprisePlugin.class);

        // Apply when all settings are evaluated
        settings.getGradle().settingsEvaluated(this::onSettingsEvaluated);

        // Apply build scanning
        final String buildScanServer = System.getProperty("org.noelware.gradle.buildScan.server", "");
        settings.getExtensions().configure(GradleEnterpriseExtension.class, (buildScan) -> {
            buildScan.buildScan((scan) -> {
                if (buildScanServer.isEmpty()) {
                    scan.setTermsOfServiceAgree("yes");
                    scan.setTermsOfServiceUrl("https://gradle.com/terms-of-service");

                    if (System.getenv("CI") != null) {
                        scan.publishAlways();
                    }
                } else {
                    scan.publishAlways();
                    scan.setServer(buildScanServer);
                }

                scan.obfuscation((obfuscation) -> {
                    obfuscation.ipAddresses((addr) -> List.of("0.0.0.0"));
                    obfuscation.hostname((h) -> "[redacted]");
                    obfuscation.username((u) -> "[redacted]");
                });
            });
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void onSettingsEvaluated(Settings settings) {
        final String name = settings.getRootProject().getName();
        if (OperatingSystem.current().isUnsupported())
            throw new GradleException(
                    """
            Project %s requires a valid installation of Windows, macOS, or Linux to be developed on,
            you are currently on %s.
            """
                            .formatted(name, System.getProperty("os.name")));

        if (Architecture.current().isUnsupported())
            throw new GradleException("Project %s is only supported on x86_64 or ARM64 systems, you're currently on %s"
                    .formatted(name, System.getProperty("os.arch")));

        final JavaVersion javaVersion = JavaVersion.current();
        boolean disableJavaSanityCheck = BOOLEAN_REGEX
                .matcher(System.getProperty("org.noelware.gradle.ignoreJavaCheck", "false"))
                .matches();

        final String disableCheck2 = System.getenv("GRADLE_DISABLE_JAVA_SANITY_CHECK");
        if (BOOLEAN_REGEX
                .matcher(disableCheck2 != null ? disableCheck2 : "false")
                .matches()) {
            disableJavaSanityCheck = true;
        }

        if (!disableJavaSanityCheck && Integer.parseInt(javaVersion.getMajorVersion()) < 17) {
            throw new GradleException(
                    """
            Project %s requires Java 17 or higher to be used when using Gradle. You're currently on
            Java %s! To disable any sanity checks (which we do not recommend), you will need
            to use either:

                * environment variable `GRADLE_DISABLE_JAVA_SANITY_CHECK` with `yes`, `true`, `1`, or `si`.
                * system property `org.noelware.gradle.ignoreJavaCheck` with `yes`, `true`, `1`, or `si`.
                * `systemProp.org.noelware.gradle.ignoreJavaCheck` with `yes`, `true`, `1`, or `si` in `gradle.properties`
            """
                            .formatted(name, javaVersion.getMajorVersion()));
        }

        final String buildCacheUri = System.getProperty("org.noelware.gradle.buildCache.url");
        final String buildCacheDir = System.getProperty("org.noelware.gradle.buildCache.dir");
        final boolean shouldOverride = buildCacheDir != null || buildCacheUri != null;

        if (shouldOverride && buildCacheUri != null) {
            final URI uri = URI.create(buildCacheUri);
            final boolean ci = System.getenv("CI") != null;

            settings.buildCache((cache) -> {
                cache.remote(HttpBuildCache.class, (remoteCache) -> {
                    remoteCache.setAllowInsecureProtocol("http".equalsIgnoreCase(uri.getScheme()));
                    remoteCache.setPush(ci);
                    remoteCache.setUrl(uri);

                    final String username = System.getProperty("org.noelware.gradle.buildCache.username");
                    if (username != null) {
                        final String password = System.getProperty("org.noelware.gradle.buildCache.password");
                        if (password == null)
                            throw new GradleException(
                                    "Missing `org.noelware.gradle.buildCache.password` system property");

                        remoteCache.credentials((credentials) -> {
                            credentials.setUsername(username);
                            credentials.setPassword(password);
                        });
                    }
                });
            });
        }

        if (shouldOverride && buildCacheDir != null) {
            final File file = new File(buildCacheDir);
            if (!file.exists()) file.exists();
            if (!file.isDirectory())
                throw new GradleException("Expected path [%s] to be a directory".formatted(buildCacheDir));

            settings.buildCache((opts) -> opts.local((local) -> {
                local.setDirectory(file.toString());
                local.setRemoveUnusedEntriesAfterDays(14);
            }));
        }
    }
}
