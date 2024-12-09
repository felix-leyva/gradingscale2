@file:Suppress("UnstableApiUsage")

rootProject.name = "GradingScale2"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://packages.jetbrains.team/maven/p/firework/dev")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs2") {
            from(files("gradle/libs.versions.toml"))
        }
    }
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://packages.jetbrains.team/maven/p/firework/dev")
    }
}
include(":composeApp")
include(":data:network")
include(":data:authFirebase")
include(":data:persistance:sharedprefs")
include(":data:persistance:db")
include(":entities")
