plugins {
    id("kmp-compose-runtime")
}

kotlin {
    androidLibrary {
        namespace = libs.versions.packagename + ".core.presentation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
        }
    }
}
