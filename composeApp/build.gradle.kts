@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    id(libs2.plugins.kotlinMultiplatform.get().pluginId)
    // AGP 9: the Android target is a KMP library; the installable app lives in :androidApp
    // (which also hosts the google-services and sentry plugins)
    id(libs2.plugins.androidMultiplatformLibrary.get().pluginId)
    id(libs2.plugins.jetbrainsCompose.get().pluginId)
    id(libs2.plugins.compose.compiler.get().pluginId)
    id(libs2.plugins.ksp.get().pluginId)
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
    alias(libs2.plugins.conveyor)
    // Compose 1.11+ puts hot-reload on the plugin classpath transitively, so apply without a version
    id(libs2.plugins.hot.reload.get().pluginId)
}

kotlin {
    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "composeApp"
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

    androidLibrary {
        // Must differ from the :androidApp namespace (the old applicationId namespace moved there)
        namespace = libs2.versions.applicationId.get() + ".shared"
        compileSdk = libs2.versions.android.compileSdk.get().toInt()
        minSdk = libs2.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs2.versions.java.get()))
        }

        // Required for Compose Multiplatform resources on the Android target
        androidResources {
            enable = true
        }

        // Unit tests need default return values for molecule's MonotonicClock
        withHostTestBuilder {}.configure {
            isReturnDefaultValues = true
        }
    }

    jvmToolchain(libs2.versions.java.get().toInt())

    // No iosX64: Compose Multiplatform 1.11 dropped the Intel Apple targets
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    // Disable iOS tests due to Firebase framework linking issues in test binaries
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        if (name.startsWith("ios")) {
            binaries.all {
                if (this is org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable) {
                    linkTaskProvider.configure {
                        enabled = false
                    }
                }
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs2.androidx.activity.compose)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3AdaptiveNavigationSuite)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs2.navigation3.ui)
            implementation(libs2.material3.adaptive)
            implementation(libs2.material3.adaptive.layout)
            implementation(libs2.material3.adaptive.navigation)
            implementation(libs2.lifecycle.viewmodel.nav3)
            implementation(libs2.haze)
            implementation(libs2.haze.materials)

            implementation(libs2.napier)
            implementation(libs2.androidx.lifecycle.viewmodel)
            implementation(libs2.androidx.lifecycle.runtime.compose)
            implementation(libs2.koin.core)
            implementation(libs2.ktor.serialization.kotlinx.json)
            implementation(libs2.koin.compose)
            implementation(libs2.koin.compose.viewmodel)
            implementation(libs2.kotlinx.collections.immutable)
            implementation(libs2.molecule.runtime)

            // Arrow
            implementation(libs2.arrow.optics)

            implementation(projects.entities)
            implementation(projects.data.authFirebase)
            implementation(projects.data.network)
            implementation(projects.data.persistance.db)
            implementation(projects.data.persistance.sharedprefs)
            implementation(projects.data.diagnostics)
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
compose.desktop {
    application {
        mainClass = libs2.versions.mainClassName.get()
        buildTypes {
            release {
                proguard {
                    isEnabled = false
                }
            }
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = libs2.versions.desktopPackageName.get()
            packageVersion = libs2.versions.appVersion.get()
            modules("java.instrument", "java.management", "java.sql", "jdk.unsupported")

            macOS {
                iconFile.set(project.file("src/commonMain/composeResources/drawable/app_icon.icns"))
                bundleID = libs2.versions.applicationId.get()
                packageName = libs2.versions.appDisplayName.get()
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

compose {
    resources {
        publicResClass = true
    }
}

dependencies {
    ksp(libs2.arrow.optics.ksp.plugin)
    // Use the configurations created by the Conveyor plugin to tell Gradle/Conveyor where to find the artifacts for each platform.
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}

// Conveyor wires the host machine's configuration (e.g. linuxAmd64) into the first of
// jvmMainImplementation/commonMainImplementation/implementation that exists when the plugin is applied.
// Before AGP 9, com.android.application provided "implementation"; now only commonMainImplementation
// exists that early, which would leak the desktop artifacts into every KMP target (they end up in the
// Android APK and break wasm dependency resolution). Move the wiring to the JVM target where it belongs.
val conveyorMachineConfigNames = setOf("linuxAmd64", "macAmd64", "macAarch64", "windowsAmd64")
val commonMainImplementation = configurations.getByName("commonMainImplementation")
val conveyorHostConfigs = commonMainImplementation.extendsFrom.filter { it.name in conveyorMachineConfigNames }
if (conveyorHostConfigs.isNotEmpty()) {
    commonMainImplementation.setExtendsFrom(commonMainImplementation.extendsFrom - conveyorHostConfigs.toSet())
    configurations.getByName("jvmMainImplementation").extendsFrom(*conveyorHostConfigs.toTypedArray())
}

// Work around https://conveyor.hydraulic.dev/17.0/tutorial/tortoise/2-gradle/#adapting-a-compose-multiplatform-app
configurations.configureEach {
    // Gradle 9 forbids setting attributes on declarable-only configurations; they only matter for resolution anyway
    if (isCanBeResolved || isCanBeConsumed) {
        attributes {
            // https://github.com/JetBrains/compose-jb/issues/1404#issuecomment-1146894731
            attribute(Attribute.of("ui", String::class.java), "awt")
        }
    }
}
