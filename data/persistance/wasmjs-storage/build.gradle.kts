@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
}

kotlin {
    applyDefaultHierarchyTemplate()

    // Only configure web targets - no need for Android, iOS, JVM!
    js {
        browser()
    }

    wasmJs {
        browser {
            testTask {
                enabled = false
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs2.kstore)
            implementation(libs2.kotlinx.serialization)
            implementation(libs2.kotlinx.coroutines.core)
        }
    }
}
