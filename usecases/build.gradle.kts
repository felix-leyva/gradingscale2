plugins {
    id("multiplatform-plugin")
    id(
        libs.plugins.kotlinxSerialization
            .get()
            .pluginId,
    )
}

kotlin {
    js {
        browser {
            testTask {
                useKarma {
                    useFirefox()
                }
            }
        }
    }
    sourceSets {

        commonMain.dependencies {
            implementation(project(":entities"))
        }
    }
}

android {
    namespace = libs.versions.packagename + ".usecases"
}
