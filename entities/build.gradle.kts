@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    id("multiplatform-plugin")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
    id(libs2.plugins.compose.compiler.get().pluginId)
    id(libs2.plugins.jetbrainsCompose.get().pluginId)
    id(libs2.plugins.gmazzoBuildConfig.get().pluginId)
}

kotlin {
    js {
        browser {
            testTask {
                onlyIf { !System.getenv().containsKey("CI") }
                useKarma {
                    useFirefox()
                }
            }
        }
        // Ensure serializers are generated for JS
        compilations.all {
            kotlinOptions {
                moduleKind = "commonjs"
            }
        }
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
            implementation(compose.runtime)
            implementation(compose.components.resources)
            implementation(libs2.molecule.runtime)
            implementation(libs2.kotlinx.collections.immutable)
            implementation(libs2.kotlinx.serialization)
        }
    }
}
compose {
    resources {
        publicResClass = true
    }
}
android {
    namespace = libs.versions.packagename + ".entities"
    // This is important to be able to run unit test when using molecule, due the MonotonicClock dependency
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}
dependencies {
    testImplementation(project(":entities"))
}

buildConfig {
    className("BuildConfigs")
    val packageName = libs2.versions.applicationId.get()
    useKotlinOutput { internalVisibility = false }
    packageName(packageName)
    buildConfigField("String", "PACKAGE_NAME", "\"$packageName\"")
    buildConfigField("String", "VERSION_CODE", "\"${libs2.versions.versionCode.get()}\"")
    buildConfigField("String", "ORGANIZATION", "\"${libs2.versions.organization.get()}\"")
}
