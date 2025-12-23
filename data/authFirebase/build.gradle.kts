@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    id("multiplatform-plugin")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
    id(libs2.plugins.gmazzoBuildConfig.get().pluginId)
}

kotlin {
    applyDefaultHierarchyTemplate()

    wasmJs {
        browser {
            testTask {
                enabled = false
            }
        }
    }
    sourceSets {
        val firebaseAvailable by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs2.firebase.auth)
            }
        }

        androidMain {
            dependsOn(firebaseAvailable)
            dependencies {
                // Workaround for https://github.com/GitLiveApp/firebase-kotlin-sdk/issues/356
                implementation(project.dependencies.platform(libs2.firebase.bom))
                implementation(libs2.firebase.common.ktx)
                implementation(libs2.firebase.auth.ktx)
            }
        }

        commonMain.dependencies {
            implementation(projects.entities)
            implementation(libs2.napier)
        }

        val wasmJsMain by getting {
            // WasmJS now has its own Firebase implementation using JS interop
            dependencies {
                // Uses Firebase loaded via CDN in index.html
            }
        }
        iosMain {
            dependsOn(firebaseAvailable)
        }
        jvmMain.get().dependsOn(firebaseAvailable)
    }
}

android {
    namespace = libs.versions.packagename + ".authfirebase"
}

// Generate Firebase constants for the JVM build which does not has a plugin to generate the configuration
// Using the extension function from buildSrc for better encapsulation
configureFirebaseBuildConfig()
