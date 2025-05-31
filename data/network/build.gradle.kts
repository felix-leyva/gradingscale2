@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("multiplatform-plugin")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
    id(libs2.plugins.ksp.get().pluginId)
    id(libs2.plugins.gmazzoBuildConfig.get().pluginId)
}

kotlin {

    js {
        outputModuleName = "network"
        browser {
            testTask {
                onlyIf { !System.getenv().containsKey("CI") }
                useKarma {
                    useFirefox()
                }
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

        androidMain.dependencies {
            implementation(libs2.ktor.client.okhttp)
        }

        commonMain.dependencies {
            implementation(libs2.ktor.client.core)
            implementation(libs2.ktor.client.content.negotiation)
            implementation(libs2.ktor.serialization.kotlinx.json)
            implementation(libs2.ktor.client.logging)
            implementation(libs2.ktor.client.auth)
            implementation(projects.entities)
        }

        commonTest.dependencies {
            implementation(libs2.ktor.client.test)
        }

        jvmMain.dependencies {
            implementation(libs2.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs2.ktor.client.darwin)
        }

        jsMain.dependencies {
            implementation(libs2.ktor.client.js)
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs2.ktor.client.js)
            }
        }
    }
}

android {
    namespace = libs.versions.packagename + ".network"
}

// Generate Firebase constants for the JVM build which does not has a plugin to generate the configuration
val buildUrls = buildConfig.forClass("BuildBaseUrls")
val generateBuildUrls by tasks.registering {
    val baseUrl = gradleLocalProperties(rootDir, providers).getProperty("GRADINGSCALE_BASE_URL")

    doFirst {
        buildUrls.apply {
            packageName("de.felixlf.gradingscale2")
            buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        }
    }
}

tasks.matching { it.name.startsWith("compile") }.configureEach {
    dependsOn(generateBuildUrls)
}
