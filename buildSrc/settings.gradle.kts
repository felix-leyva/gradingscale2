@file:Suppress("UnstableApiUsage")

rootProject.name = "buildSrc"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        // Required for sqlDelight
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
}

plugins {
    id("de.felixlf.libs-catalog-generator") version "1.1"
}
