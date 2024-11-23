plugins {
    id("multiplatform-plugin")
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
            implementation(libs2.kotlinx.collections.immutable)
        }
    }
}

android {
    namespace = libs.versions.packagename + ".entities"
}
dependencies {
    testImplementation(project(":entities"))
}
