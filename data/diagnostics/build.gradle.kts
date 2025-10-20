@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    id("multiplatform-plugin")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
    id(libs2.plugins.gmazzoBuildConfig.get().pluginId)
    id(libs2.plugins.sentry.get().pluginId)
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
        commonMain.dependencies {
            implementation(projects.entities)
        }

        val sentryAvailable by creating {
            dependsOn(commonMain.get())
            dependencies {
            }
        }

        androidMain {
            dependsOn(sentryAvailable)
        }

        commonMain.dependencies {
            implementation(projects.entities)
        }

        val webMain by getting {
            dependencies {
                // Sentry is here an empty no-implementation
            }
        }
        iosMain {
            dependsOn(sentryAvailable)
        }
        jvmMain.get().dependsOn(sentryAvailable)
    }
}

android {
    namespace = libs.versions.packagename + ".diagnostics"
}

buildConfig {
    buildConfigField(
        "SENTRY_DSN",
        System.getenv("SENTRY_DSN") ?: throw NullPointerException("SENTRY_DSN environmental variable not set yet"),
    )
}
