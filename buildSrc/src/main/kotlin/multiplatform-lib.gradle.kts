@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    applyDefaultHierarchyTemplate()

    wasmJs {
        browser()
    }

    // AGP 9: the Android target is configured here instead of a top-level android {} block.
    // Modules set their namespace in their own kotlin.androidLibrary {} block.
    androidLibrary {
        compileSdk = libs.versions.androidCompileSdk.toInt()
        minSdk = libs.versions.androidMinSdk.toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.java))
        }

        // Required for Compose Multiplatform resources on the Android target
        androidResources {
            enable = true
        }

        packaging {
            resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }

        // Host-side unit tests; default return values are needed for molecule's MonotonicClock
        withHostTestBuilder {}.configure {
            isReturnDefaultValues = true
        }
    }

    jvm()
    jvmToolchain(libs.versions.java.toInt())

    // No iosX64: Compose Multiplatform 1.11 dropped the Intel Apple targets
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )
}
