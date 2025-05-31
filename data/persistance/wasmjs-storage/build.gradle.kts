@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    id("gs-android-library")
    id("org.jetbrains.kotlin.multiplatform")
    id("multiplatform-plugin")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
}

kotlin {
    js {
        browser()
    }

    wasmJs {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs2.kstore)
                implementation(libs2.kotlinx.serialization)
                implementation(libs2.kotlinx.coroutines.core)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                // WasmJS specific dependencies if needed
            }
        }
    }
}

android {
    namespace = libs.versions.packagename + ".storage"
}
