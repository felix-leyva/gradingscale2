@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    id("gs-android-library")
    id("multiplatform-plugin")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
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
        commonMain.dependencies {
            implementation(project(":entities"))
            implementation(libs2.kstore)
            implementation(libs2.kotlinx.serialization)
        }
        androidMain.dependencies {
            implementation(libs2.kstore.file)
        }
        iosMain.dependencies {
            implementation(libs2.kstore.file)
        }
        jvmMain.dependencies {
            implementation(libs2.kstore.file)
            implementation(libs2.appdirs)
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
android {
    namespace = libs.versions.packagename + ".sharedprefs"
}
