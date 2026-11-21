@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
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

    androidLibrary {
        compileSdk = libs.versions.androidCompileSdk.toInt()
        minSdk = libs.versions.androidMinSdk.toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.java))
        }

        androidResources {
            enable = false
        }

        packaging {
            resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }

        withHostTestBuilder {}.configure {
            isReturnDefaultValues = true
        }
    }

    jvm()
    jvmToolchain(libs.versions.java.toInt())

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )

    sourceSets {
        commonMain.dependencies {
            implementation(libs.libraries.kotlinxCoroutinesCore)
            implementation(libs.libraries.arrowCore)
            implementation(libs.libraries.kotlinxCollectionsImmutable)
        }

        commonTest.dependencies {
            implementation(libs.libraries.junit)
            implementation(libs.libraries.kotlinTest)
            implementation(libs.libraries.turbine)
            implementation(libs.libraries.coroutinesTest)
        }
    }
}
