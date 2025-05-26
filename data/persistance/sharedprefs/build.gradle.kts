plugins {
    id("gs-android-library")
    id("multiplatform-plugin")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
}
kotlin {
    js {
        outputModuleName = "sharedprefs"
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
    sourceSets {
        commonMain.dependencies {
            implementation(project(":entities"))
            implementation(libs2.kstore)
            implementation(libs2.kotlinx.serialization)
        }
        androidMain.dependencies {
            implementation(libs2.kstore.file)
        }
        iosMain.dependencies {
            implementation(libs2.kstore.file)
        }
        jvmMain.dependencies {
            implementation(libs2.kstore.file)
            implementation(libs2.appdirs)
        }
        jsMain.dependencies {
            implementation(libs2.kstore.storage)
        }
    }
}
android {
    namespace = libs.versions.packagename + ".sharedprefs"
}
