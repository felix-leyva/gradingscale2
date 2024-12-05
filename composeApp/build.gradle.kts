import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(
        libs.plugins.kotlinMultiplatform
            .get()
            .pluginId,
    )
    id("gs-android-app")
    id(
        libs.plugins.google.services
            .get()
            .pluginId,
    )
    id(
        libs.plugins.jetbrainsCompose
            .get()
            .pluginId,
    )
    id(
        libs.plugins.compose.compiler
            .get()
            .pluginId,
    )
    id(
        libs.plugins.ksp
            .get()
            .pluginId,
    )
    id(
        libs.plugins.kotlinxSerialization
            .get()
            .pluginId,
    )
}

kotlin {
    js(IR) {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
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
//        }
//        binaries.executable()
//    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")

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
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs2.androidx.activity.compose)
            implementation(libs2.ktor.client.okhttp)
            implementation(libs2.koin.android)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs2.androidx.lifecycle.viewmodel)
            implementation(libs2.androidx.lifecycle.runtime.compose)
            implementation(libs2.ktor.client.core)
            implementation(libs2.ktor.client.content.negotiation)
            implementation(libs2.ktor.serialization.kotlinx.json)
            implementation(libs2.koin.core)
            implementation(libs2.koin.compose)
            implementation(libs2.koin.compose.viewmodel)
            implementation(libs2.koin.compose.viewmodel.nav)
            implementation(libs2.kotlinx.collections.immutable)
            implementation(project(":entities"))
            implementation(project(":data:authFirebase"))
            implementation(project(":data:network"))
            implementation(project(":data:persistance:db"))
        }

        commonTest.dependencies {
            implementation(libs2.junit)
            implementation(libs2.kotlin.test)
            implementation(libs2.koin.test)
        }

        desktopMain.dependencies {
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
