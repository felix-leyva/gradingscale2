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
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://packages.jetbrains.team/maven/p/firework/dev")
    }
}

plugins {
    id("de.felixlf.libs-catalog-generator") version "2.0"
}

catalogGenerator {
    catalogName = "libs"
}
