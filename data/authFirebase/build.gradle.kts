plugins {
    id("multiplatform-plugin")
    id(
        libs.plugins.kotlinxSerialization
            .get()
            .pluginId,
    )
}

kotlin {
    applyDefaultHierarchyTemplate()

    js {
        moduleName = "authfirebase"
        browser()
    }
    sourceSets {
        val firebaseAvailable by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs2.firebase.auth)
                implementation(libs2.firebase.analytics)
            }
        }        
        
        androidMain {
            dependsOn(firebaseAvailable)
            dependencies {
                // Workaround for https://github.com/GitLiveApp/firebase-kotlin-sdk/issues/356
                implementation(project.dependencies.platform(libs2.firebase.bom))
                implementation(libs2.firebase.common.ktx)
                implementation(libs2.firebase.auth.ktx)
    //            implementation(libs2.firebase.crashlytics)
            }
        }


        commonMain {

        }

        jsMain.dependencies {
//            implementation(npm("@gitliveapp/firebase-auth", "1.5.19-beta"))
//            implementation(npm("@gitliveapp/firebase-common", "1.5.19-beta"))

        }
        iosMain {
            dependsOn(firebaseAvailable)
            dependencies {
                implementation(libs2.firebase.crashlytics)
            }
        }
        jvmMain.get().dependsOn(firebaseAvailable)
    }
}

android {
    namespace = libs.versions.packagename + ".authfirebase"
}
