@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    id(
        libs2.plugins.kotlinMultiplatform.get().pluginId,
    )
    id("gs-android-app")
    id(
        libs2.plugins.google.services.get().pluginId,
    )
    id(
        libs2.plugins.jetbrainsCompose.get().pluginId,
    )
    id(
        libs2.plugins.compose.compiler.get().pluginId,
    )
    id(
        libs2.plugins.ksp.get().pluginId,
    )
    id(
        libs2.plugins.kotlinxSerialization.get().pluginId,
    )
    alias(libs2.plugins.conveyor)
}

kotlin {
    version = "1.0"

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = provider { "composeApp" }
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs2.versions.java.get()))
        }
    }

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
            implementation(libs2.koin.android)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3)
            implementation(compose.material3AdaptiveNavigationSuite)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs2.navigation.compose)
            implementation(libs2.material3.window.size)

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
            implementation(libs2.arrow.optics)

            implementation(project(":entities"))
            implementation(project(":data:authFirebase"))
            implementation(project(":data:network"))
            implementation(project(":data:persistance:db"))
            implementation(project(":data:persistance:sharedprefs"))
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

            // Enable by default, disable with: -Pcompose.desktop.production=true
            if (project.findProperty("compose.desktop.production")?.toString()?.toBoolean() != true) {
                implementation(libs2.slf4j.simple)
            }
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
        buildTypes {
            release {
                proguard {
                    isEnabled = false
                }
            }
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "grading-scale"
            packageVersion = "1.0.0"
            modules("java.instrument", "java.management", "java.sql", "jdk.unsupported")

            macOS {
                iconFile.set(project.file("src/commonMain/composeResources/drawable/app_icon.icns"))
                bundleID = "de.felixlf.gradingscale2"
                packageName = "Grading Scale"
                appStore = true
                appCategory = "public.app-category.utilities"
            }
            windows {
                iconFile.set(project.file("src/commonMain/composeResources/drawable/app_icon.ico"))
                perUserInstall = true
            }
            linux {
                iconFile.set(project.file("src/commonMain/composeResources/drawable/app_icon.png"))
            }
        }
    }
}

composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}

compose {
    resources {
        publicResClass = true
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

tasks.register("checkAndCreateIosGoogleServices") {
    val googleServicesFile = parent?.layout?.projectDirectory?.file("iosApp/iosApp/GoogleService-Info.plist")
        ?: run {
            println("Parent project not found.")
            return@register
        }
    val googleServicesContent = providers.environmentVariable("GOOGLE_IOS_SECRET")
    doLast {
        if (!googleServicesFile.asFile.exists()) {
            val content = googleServicesContent.orNull
            if (content != null) {
                googleServicesFile.asFile.writeText(content)
                println("GoogleService-Info.plist file created")
            } else {
                println("Environment variable GOOGLE_IOS_SECRET is not set.")
            }
        } else {
            println("GoogleService-Info.plist file already exists.")
        }
    }
}

tasks.named("preBuild") {
    dependsOn("checkAndCreateGoogleServices", "checkAndCreateIosGoogleServices")
}


dependencies {
    ksp(libs2.arrow.optics.ksp.plugin)
    // Use the configurations created by the Conveyor plugin to tell Gradle/Conveyor where to find the artifacts for each platform.
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}

// Work around https://conveyor.hydraulic.dev/17.0/tutorial/tortoise/2-gradle/#adapting-a-compose-multiplatform-app
configurations.all {
    attributes {
        // https://github.com/JetBrains/compose-jb/issues/1404#issuecomment-1146894731
        attribute(Attribute.of("ui", String::class.java), "awt")
    }
}
