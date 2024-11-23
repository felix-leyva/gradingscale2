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
        moduleName = "usecases"
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
