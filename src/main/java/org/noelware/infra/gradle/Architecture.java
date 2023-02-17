/*
 * gradle-infra-plugin: Gradle plugin to configure sane defaults for Noelware's Gradle projects
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the host CPU architecture
 */
public enum Architecture {
    /**
     * CPU architecture is using the <code>x86_64</code>/<code>amd64</code> architecture
     */
    X64,

    /**
     * CPU architecture is using the <code>ARM64</code> architecture
     */
    ARM64,

    /**
     * CPU architecture that is not supported and has not been tested with this plugin
     */
    UNSUPPORTED;

    /**
     * @return the host cpu's architecture
     */
    @NotNull
    public static Architecture current() {
        final String arch = System.getProperty("os.arch");
        return switch (arch) {
            case "x86_64", "amd64" -> X64;
            case "aarch64", "arm64" -> ARM64;
            default -> UNSUPPORTED;
        };
    }

    /**
     * @return the quantifiable name for this {@link Architecture}, or <code>null</code> if this
     * is <code>UNSUPPORTABLE</code>
     */
    @Nullable
    public String getName() {
        return switch (this) {
            case UNSUPPORTED -> null;
            case ARM64 -> "arm64";
            case X64 -> "x86_64";
        };
    }

    /**
     * @return if the host CPU architecture is x86_64
     */
    public boolean isX64() {
        return this == X64;
    }

    /**
     * @return if the host CPU architecture is ARM64
     */
    public boolean isArm64() {
        return this == ARM64;
    }

    /**
     * @return if this is an unsupported operating system
     */
    public boolean isUnsupported() {
        return this == UNSUPPORTED;
    }

    @Override
    public String toString() {
        return "OperatingSystem(%s)".formatted(getName() == null ? "unsupported" : getName());
    }
}
