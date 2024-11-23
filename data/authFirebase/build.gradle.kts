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
        moduleName = "authfirebase"
        browser()
    }
    sourceSets {
        androidMain.dependencies {
            // Workaround for https://github.com/GitLiveApp/firebase-kotlin-sdk/issues/356
            implementation(project.dependencies.platform(libs2.firebase.bom))
            implementation(libs2.firebase.common.ktx)
            implementation(libs2.firebase.auth.ktx)
//            implementation(libs2.firebase.crashlytics)
        }

        commonMain.dependencies {
            implementation(libs2.firebase.auth)
            implementation(libs2.firebase.analytics)
        }

        jsMain.dependencies {
        }
        iosMain.dependencies {
            implementation(libs2.firebase.crashlytics)
        }
    }
}

android {
    namespace = libs.versions.packagename + ".authfirebase"
}
