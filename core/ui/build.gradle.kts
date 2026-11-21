plugins {
    id("kmp-compose-feature-impl")
    id(libs2.plugins.kotlinxSerialization.get().pluginId)
}

kotlin {
    androidLibrary {
        namespace = libs.versions.packagename + ".core.ui"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(libs2.kotlinx.serialization)
            implementation(libs2.material3.adaptive)
            implementation(libs2.material3.adaptive.layout)
            implementation(libs2.material3.adaptive.navigation)
        }
    }
}

compose {
    resources {
        publicResClass = true
        packageOfResClass = "de.felixlf.gradingscale2.core.ui.resources"
    }
}
