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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.NotNull;
import org.noelware.infra.gradle.plugins.module.JavaModulePlugin;
import org.noelware.infra.gradle.plugins.module.NoelwareModuleExtension;

/**
 * A plugin that is meant to be used with library projects. This configures the {@link JavaModulePlugin} alongside
 * with this, but, this should be only for projects that are Java libraries.
 */
public class JavaLibraryPlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) {
        project.getPlugins().apply(JavaModulePlugin.class);
        project.getPlugins().apply("java-library");
        project.getPlugins().apply("maven-publish");

        final NoelwareModuleExtension ext = project.getExtensions().findByType(NoelwareModuleExtension.class) != null
                ? project.getExtensions().findByType(NoelwareModuleExtension.class)
                : project.getExtensions().create("noelware", NoelwareModuleExtension.class);

        assert ext != null : "extension couldn't be created";
        final TaskProvider<Jar> javadocJar = project.getTasks().register("javadocJar", Jar.class, (jar) -> {
            final Javadoc javadocTask = (Javadoc) project.getTasks().getByName("javadoc");

            jar.setDescription("Assemble Java documentation with Javadoc");
            jar.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
            jar.getArchiveClassifier().set("javadoc");
            jar.from(javadocTask);
            jar.dependsOn(javadocTask);
        });

        LibraryUtils.configurePublishing(project, "java", javadocJar, ext);
    }
}
