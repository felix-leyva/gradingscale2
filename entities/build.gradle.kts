plugins {
    id("multiplatform-plugin")
}

kotlin {
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
