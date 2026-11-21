plugins {
    id("kmp-compose-runtime")
}

kotlin {
    androidLibrary {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.libraries.composePreviewTooling)
        }

        commonMain.dependencies {
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.libraries.composeUiToolingPreview)
            implementation(libs.libraries.androidxLifecycleRuntimeCompose)
            implementation(libs.libraries.koinCompose)
            implementation(libs.libraries.koinComposeViewmodel)
            implementation(libs.libraries.haze)
            implementation(libs.libraries.hazeMaterials)
            implementation(libs.libraries.napier)
        }
    }
}
