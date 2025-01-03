plugins {
    id("gs-android-library")
    id("org.jetbrains.kotlin.multiplatform")
    id("multiplatform-plugin")
    id(
        libs2.plugins.sqldelight
            .get()
            .pluginId,
    )
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
    }
    sourceSets {
        androidMain.dependencies {
            implementation(libs2.android.driver)
        }

        nativeMain.dependencies {
            implementation(libs2.native.driver)
        }

        commonMain.dependencies {
            implementation(project(":entities"))
            implementation(libs2.kotlinx.collections.immutable)
            implementation(libs2.coroutines.extensions)
            implementation(libs2.sqldelight.runtime)
        }

        jvmMain.dependencies {
            implementation(libs2.sqlite.driver)
        }

        jsMain.dependencies {
            implementation(libs2.web.worker.driver)
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
            implementation(npm("sql.js", "1.6.2"))
        }
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set(libs.versions.applicationId)
            generateAsync.set(true)
        }
    }
}

android {
    namespace = libs.versions.packagename + ".db"
}
