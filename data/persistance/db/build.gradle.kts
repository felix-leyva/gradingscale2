@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    id("gs-android-library")
    id("org.jetbrains.kotlin.multiplatform")
    id("multiplatform-plugin")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
    id(
        libs2.plugins.sqldelight
            .get()
            .pluginId,
    )
}

kotlin {

    wasmJs {
        browser {
            testTask {
                enabled = false
            }
        }
    }
    sourceSets {
        androidMain.dependencies {
            implementation(libs2.android.driver)
        }

        nativeMain.dependencies {
            implementation(libs2.native.driver)
        }

        commonMain.dependencies {
            implementation(project(":entities"))
            implementation(libs2.kotlinx.collections.immutable)
            implementation(libs2.coroutines.extensions)
            implementation(libs2.sqldelight.runtime)
            implementation(libs2.kotlinx.serialization)
        }

        jvmMain.dependencies {
            implementation(libs2.sqlite.driver)
        }

        val wasmJsMain by getting {
            dependencies {
                // Use basic kstore without storage dependency for WasmJS
                implementation(libs2.kstore)
                implementation(project(":data:persistance:wasmjs-storage"))
            }
        }
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set(libs.versions.applicationId)
            generateAsync.set(true)
        }
    }
}

android {
    namespace = libs.versions.packagename + ".db"
}
