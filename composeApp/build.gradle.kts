@file:Suppress("UnstableApiUsage")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

// import org.jetbrains.compose.reload.ComposeHotRun

plugins {
    id(
        libs2.plugins.kotlinMultiplatform
            .get()
            .pluginId,
    )
    id("gs-android-app")
    id(
        libs2.plugins.google.services
            .get()
            .pluginId,
    )
    id(
        libs2.plugins.jetbrainsCompose
            .get()
            .pluginId,
    )
    id(
        libs2.plugins.compose.compiler
            .get()
            .pluginId,
    )
    id(
        libs2.plugins.ksp
            .get()
            .pluginId,
    )
    id(
        libs2.plugins.kotlinxSerialization
            .get()
            .pluginId,
    )
    // We add here alias, due that we do not add this buildSrc, as the Kotlin version would be enforced also there to 2.1
    // alias(libs2.plugins.hot.reload)
}

kotlin {
    js {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                    }
                }
            }
            testTask {
                onlyIf { !System.getenv().containsKey("CI") }
                useKarma {
                    useFirefox()
                }
            }
        }
        binaries.executable()
        useEsModules()
    }

//    wasmJs {
//        moduleName = "composeApp"
//        browser {
//            commonWebpackConfig {
//                outputFileName = "composeApp.js"
//                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
//                    static = (static ?: mutableListOf()).apply {
//                        // Serve sources to debug inside browser
//                        add(project.projectDir.path)
//                    }
//                }
//            }
//            testTask {
//                onlyIf { !System.getenv().containsKey("CI") }
//                useKarma {
//                    useFirefox()
//                }
//            }
//        }
//        useCommonJs()
//        binaries.executable()
//    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs2.versions.java.get()))
        }
    }

    jvm()
    jvmToolchain(libs2.versions.java.get().toInt())

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs2.androidx.activity.compose)
            implementation(libs2.ktor.client.okhttp)
            implementation(libs2.koin.android)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(libs2.material3.adaptive.navigation.suite)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs2.navigation.compose)

            implementation(libs2.napier)
            implementation(libs2.androidx.lifecycle.viewmodel)
            implementation(libs2.androidx.lifecycle.runtime.compose)
            implementation(libs2.koin.core)
            implementation(libs2.ktor.serialization.kotlinx.json)
            implementation(libs2.koin.compose)
            implementation(libs2.koin.compose.viewmodel)
            implementation(libs2.koin.compose.viewmodel.nav)
            implementation(libs2.kotlinx.collections.immutable)
            implementation(libs2.molecule.runtime)

            // Arrow
            implementation(libs2.arrow.core)
            implementation(libs2.arrow.optics)

            implementation(project(":entities"))
            implementation(project(":data:authFirebase"))
            implementation(project(":data:network"))
            implementation(project(":data:persistance:db"))
        }

        commonTest.dependencies {
            implementation(libs2.junit)
            implementation(libs2.kotlin.test)
            implementation(libs2.koin.test)
            implementation(libs2.turbine)
            implementation(libs2.coroutines.test)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs2.kotlinx.coroutines.swing)
            implementation(libs2.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs2.ktor.client.darwin)
        }
    }
}
android {
    namespace = "de.felixlf.gradingscale2"
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

compose.desktop {
    application {
        mainClass = "de.felixlf.gradingscale2.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "de.felixlf.gradingscale2"
            packageVersion = "1.0.0"
        }
    }
}

composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}
// // build.gradle.kts
// tasks.register<ComposeHotRun>("runHot") {
//    mainClass.set("de.felixlf.gradingscale2.MainKt")
// }

tasks.register("checkAndCreateGoogleServices") {
    val googleServicesFile = layout.projectDirectory.file("google-services.json")
    val googleServicesContent = providers.environmentVariable("GOOGLE_SERVICES")

    doLast {
        if (!googleServicesFile.asFile.exists()) {
            val content = googleServicesContent.orNull
            if (content != null) {
                googleServicesFile.asFile.writeText(content)
                println("google-services.json file created")
            } else {
                println("Environment variable GOOGLE_SERVICES is not set.")
            }
        } else {
            println("google-services.json file already exists.")
        }
    }
}

tasks.named("preBuild") {
    dependsOn("checkAndCreateGoogleServices")
}
dependencies {
    ksp(libs2.arrow.optics.ksp.plugin)
}
