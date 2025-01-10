plugins {
    id("multiplatform-plugin")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
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
        commonMain.dependencies {
            implementation(libs2.kotlinx.collections.immutable)
            implementation(libs2.kotlinx.serialization)
        }
    }
}

android {
    namespace = libs.versions.packagename + ".entities"
}
dependencies {
    testImplementation(project(":entities"))
}
