plugins {
    id("kmp-library-pure")
}

kotlin {
    androidLibrary {
        namespace = libs.versions.packagename + ".core.usecases.api"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
        }
    }
}
