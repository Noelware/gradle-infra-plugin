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

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Represents the available license templates
 */
public enum Licenses {
    /**
     * Apache 2.0 License
     */
    APACHE("Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0"),

    /**
     * MIT License
     */
    MIT("MIT License", "https://mit-license.org");

    private String name;
    private String url;

    Licenses(String name, String url) {
        this.name = name;
        this.url = url;
    }

    /**
     * @return the quantifiable name of this license
     */
    public String getName() {
        return name;
    }

    /**
     * @return License URL
     */
    public String url() {
        return url;
    }

    /**
     * Generates a license heading.
     * @param name The name of the project
     * @param description The description of this project
     * @param currentYear Current year
     * @param emoji project emoji
     * @return license heading
     * @throws IOException If we couldn't create a {@link InputStream} of the template
     */
    public String getTemplate(String name, String description, String currentYear, String emoji) throws IOException {
        final String tmplFile =
                switch (this) {
                    case APACHE -> "/templates/apache.heading.tmpl";
                    case MIT -> "/templates/mit.heading.tmpl";
                };

        try (final InputStream stream = Objects.requireNonNull(getClass().getResourceAsStream(tmplFile))) {
            String result = new String(stream.readAllBytes())
                    .replace("{{ Name }}", name)
                    .replace("{{ Description }}", description)
                    .replace("{{ CurrentYear }}", currentYear);

            if (!emoji.isBlank()) result = result.replace("{{ Emoji }}", emoji);
            return result;
        }
    }
}
