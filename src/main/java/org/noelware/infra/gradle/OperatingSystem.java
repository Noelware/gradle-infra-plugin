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
 * Represents the supported operating system that the host is running on.
 */
public enum OperatingSystem {
    /**
     * Host is running Windows
     */
    WINDOWS,

    /**
     * Host is running macOS
     */
    MACOS,

    /**
     * Host is running Linux
     */
    LINUX,

    /**
     * Host is running on an unsupported operating system that hasn't been tested
     * with this plugin.
     */
    UNSUPPORTED;

    /**
     * Returns the current operating system that this host is running on
     * @return the host {@link OperatingSystem} if any
     */
    @NotNull
    public static OperatingSystem current() {
        final String os = System.getProperty("os.name");
        if (os.equals("Linux")) return LINUX;
        if (os.equals("Mac OS X")) return MACOS;
        if (os.startsWith("Windows")) return WINDOWS;

        return UNSUPPORTED;
    }

    /**
     * @return the quantifiable name for this {@link OperatingSystem}, or <code>null</code> if this
     * is <code>UNSUPPORTABLE</code>
     */
    @Nullable
    public String getName() {
        return switch (this) {
            case UNSUPPORTED -> null;
            case WINDOWS -> "windows";
            case MACOS -> "macos";
            case LINUX -> "linux";
        };
    }

    /**
     * @return if this is an unsupported operating system
     */
    public boolean isUnsupported() {
        return this == UNSUPPORTED;
    }

    /**
     * @return if the host operating system is Windows
     */
    public boolean isWindows() {
        return this == WINDOWS;
    }

    /**
     * @return if the host operating system is macOS
     */
    public boolean isMacOS() {
        return this == MACOS;
    }

    /**
     * @return if the host operating system is Linux
     */
    public boolean isLinux() {
        return this == LINUX;
    }

    /**
     * @return if the host operating system is a Unix-based system
     */
    public boolean isUnix() {
        return isLinux() || isMacOS();
    }

    @Override
    public String toString() {
        return "OperatingSystem(%s)".formatted(getName() == null ? "unsupported" : getName());
    }
}
