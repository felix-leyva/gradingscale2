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
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://packages.jetbrains.team/maven/p/firework/dev")
    }
}

plugins {
    id("de.felixlf.libs-catalog-generator") version "1.3"
}
