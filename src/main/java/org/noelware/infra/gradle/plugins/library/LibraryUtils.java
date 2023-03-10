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

package org.noelware.infra.gradle.plugins.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import org.apache.commons.text.CaseUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.credentials.AwsCredentials;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.noelware.infra.gradle.plugins.module.NoelwareModuleExtension;

public class LibraryUtils {
    @ApiStatus.Internal
    public static void configurePublishing(
            @NotNull Project project,
            @NotNull String pluginSrc,
            @NotNull TaskProvider<Jar> jarTaskProvider,
            @NotNull NoelwareModuleExtension ext) {
        // Get the `publishing.properties` file from the `gradle/` directory
        // in the root project.
        final File publishingPropsFile =
                new File(project.getRootProject().getProjectDir(), "gradle/publishing.properties");

        final Properties publishingProps = new Properties();
        if (publishingPropsFile.exists()) {
            try (final FileInputStream is = new FileInputStream(publishingPropsFile)) {
                publishingProps.load(is);
            } catch (IOException e) {
                throw new GradleException("received i/o exception when creating publishing.properties container", e);
            }
        } else {
            final String accessKeyId = System.getenv("NOELWARE_PUBLISHING_ACCESS_KEY");
            final String secretAccessKey = System.getenv("NOELWARE_PUBLISHING_SECRET_KEY");

            if ((accessKeyId != null && !accessKeyId.isBlank())
                    && (secretAccessKey != null && !secretAccessKey.isBlank())) {
                publishingProps.setProperty("s3.accessKey", accessKeyId);
                publishingProps.setProperty("s3.secretKey", secretAccessKey);
            }
        }

        final SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        final String SOURCE_JAR_NAME = "%sSourcesJar".formatted(pluginSrc);
        final TaskProvider<Jar> sourcesJar = project.getTasks().register(SOURCE_JAR_NAME, Jar.class, (jar) -> {
            jar.getArchiveClassifier().set("sources");
            jar.from(sourceSets.named("main", SourceSet.class).get().getAllSource());
        });

        final String publicationName = ext.getMavenPublicationName()
                .getOrElse(
                        ext.getProjectName().getOrElse(project.getRootProject().getName()));

        project.getExtensions().configure(PublishingExtension.class, (publishing) -> {
            publishing.publications((publications) -> {
                // Create the publication
                //
                // We have most of this empty, so we let the project do that instead
                // of the plugin.
                //
                // If the publication name exists, we will extend it and rename the publications
                // to "<name>[Java|Kotlin]" (i.e, gradleInfraJava). Otherwise, we will just use
                // the publication name specified.
                final Optional<Publication> publication = publications.stream()
                        .filter(f -> f.getName().equals(publicationName))
                        .findAny();
                if (publication.isPresent()) {
                    publications.removeIf(f -> f.getName().equals(publicationName));

                    final String pubName = CaseUtils.toCamelCase(publicationName + pluginSrc, false, '-', ' ', '_');
                    publications.create(pubName, MavenPublication.class, (pp) -> {
                        pp.from(project.getComponents().getByName(pluginSrc));
                        pp.artifact(sourcesJar.get());
                        pp.artifact(jarTaskProvider.get());
                    });
                } else {
                    publications.create(publicationName, MavenPublication.class, (pub) -> {
                        pub.from(project.getComponents().getByName(pluginSrc));

                        pub.artifact(sourcesJar.get());
                        pub.artifact(jarTaskProvider.get());
                    });
                }
            });

            publishing.repositories((repositories) -> {
                repositories.maven((maven) -> {
                    maven.setUrl(ext.getS3BucketUrl().getOrElse("s3://august/noelware/maven"));
                    maven.credentials(AwsCredentials.class, (creds) -> {
                        creds.setAccessKey(publishingProps.getProperty("s3.accessKey"));
                        creds.setSecretKey(publishingProps.getProperty("s3.secretKey"));
                    });
                });
            });
        });
    }
}
