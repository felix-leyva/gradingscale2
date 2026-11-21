plugins {
    id("kmp-library-di")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(libs.libraries.moleculeRuntime)
            implementation(libs.libraries.androidxLifecycleViewmodel)
        }
    }
}
