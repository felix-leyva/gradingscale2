plugins {
    id("multiplatform-plugin")
    id(
        libs2.plugins.kotlinxSerialization
            .get()
            .pluginId,
    )
    id(
        libs2.plugins.ksp
            .get()
            .pluginId,
    )
}

kotlin {

    js {
        outputModuleName = "network"
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
            implementation(libs2.ktor.client.okhttp)
        }

        commonMain.dependencies {
            implementation(libs2.ktor.client.core)
            implementation(libs2.ktor.client.content.negotiation)
            implementation(libs2.ktor.serialization.kotlinx.json)
        }

        jvmMain.dependencies {
            implementation(libs2.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs2.ktor.client.darwin)
        }
    }
}

android {
    namespace = libs.versions.packagename + ".network"
}
