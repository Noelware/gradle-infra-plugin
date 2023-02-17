# 🐻‍❄️🐘 Noelware Gradle Plugin
> *Gradle plugin to configure sane defaults for Noelware's Gradle projects*

**gradle-infra** is Noelware's Gradle plugin that is applied on all Noelware's Java and Kotlin projects to not repeat ourselves when building new
products and services in Kotlin or Java that can be extended and shared across all of our projects.

Most of the configuration is taken from [charted-dev/charted@f45347f5](https://github.com/charted-dev/charted/blob/f45347f5bd5b34ecb17757b5353794a404dbf23b).

## Usage
This plugin comes with multiple plugins that suite what we are trying to build:

- `org.noelware.gradle.kotlin` (extends from [**Plugin**](https://docs.gradle.org/current/javadoc/org/gradle/api/Plugin.html)<[**Project**](https://docs.gradle.org/current/dsl/org.gradle.api.Project.html)>)
- `org.noelware.gradle.kotlin-library` (extends from [**Plugin**](https://docs.gradle.org/current/javadoc/org/gradle/api/Plugin.html)<[**Project**](https://docs.gradle.org/current/dsl/org.gradle.api.Project.html)>)
- `org.noelware.gradle.java` (extends from [**Plugin**](https://docs.gradle.org/current/javadoc/org/gradle/api/Plugin.html)<[**Project**](https://docs.gradle.org/current/dsl/org.gradle.api.Project.html)>)
- `org.noelware.gradle.java-library` (extends from [**Plugin**](https://docs.gradle.org/current/javadoc/org/gradle/api/Plugin.html)<[**Project**](https://docs.gradle.org/current/dsl/org.gradle.api.Project.html)>)
- `org.noelware.gradle.settings` (extends from [**Plugin**](https://docs.gradle.org/current/javadoc/org/gradle/api/Plugin.html)<[**Settings**](https://docs.gradle.org/current/dsl/org.gradle.api.initialization.Settings.html)>)

```kotlin
buildscript {
    repositories {
        maven("https://maven.noelware.org")
        gradlePluginPortal()
        mavenCentral()
    }
  
    dependencies { 
        classpath("org.noelware.gradle:infra-gradle-plugin:1.0.0") 
    }
}

plugins { 
    id("org.noelware.gradle.kotlin") version "1.0.0"
}

noelware {
    license.set(org.noelware.infra.gradle.Licenses.MIT)
    currentYear.set("2022-2023")
    projectName.set("my project name")
}
```

## License
**gradle-infra** is released under the **MIT License** with love by [Noelware](https://noelware.org)! 🐻‍❄️💜
