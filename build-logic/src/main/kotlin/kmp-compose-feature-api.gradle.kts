plugins {
    id("kmp-library-pure")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.libraries.navigation3Ui)
            implementation(libs.libraries.kotlinxSerialization)
        }
    }
}
