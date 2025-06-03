@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("gs-android-library")
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    wasmJs {
        browser()
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.java))
        }
    }

    jvm()
    jvmToolchain(libs.versions.java.toInt())

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    )
}
